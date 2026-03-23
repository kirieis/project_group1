#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
JMeter Stress Test Reporter - Real-time monitoring & HTML report generation
Connects to Apache JMeter results and updates stress-test-report.html
"""

import json
import csv
import os
import sys
import time
import subprocess
import argparse
import xml.etree.ElementTree as ET
from pathlib import Path
from datetime import datetime
from collections import defaultdict

# Config
CONFIG_FILE = "jmeter-reporter.config.json"
SCRIPT_DIR = Path(__file__).parent.resolve()


def load_config():
    cfg_path = SCRIPT_DIR / CONFIG_FILE
    if not cfg_path.exists():
        raise FileNotFoundError(f"Config not found: {cfg_path}")
    with open(cfg_path, "r", encoding="utf-8") as f:
        return json.load(f)


def _parse_jtl_csv(jtl_path):
    """Parse JMeter CSV/JTL format."""
    samples = []
    with open(jtl_path, "r", encoding="utf-8", errors="replace") as f:
        first_line = f.readline().strip()
        delimiter = "|" if "|" in first_line and "," not in first_line.split("|")[0] else ","
        f.seek(0)
        reader = csv.DictReader(f, delimiter=delimiter)
        for row in reader:
            try:
                elapsed = int(row.get("elapsed", row.get("time", 0)))
                success = str(row.get("success", "true")).lower() == "true"
                ts = int(row.get("timeStamp", 0))
                bytes_val = int(row.get("bytes", 0))
                samples.append({"elapsed": elapsed, "success": success, "timestamp": ts, "bytes": bytes_val})
            except (ValueError, KeyError):
                continue
    return samples


def _parse_jtl_xml(jtl_path):
    """Parse JMeter XML format (Simple Data Writer default)."""
    samples = []
    try:
        tree = ET.parse(jtl_path)
        root = tree.getroot()
        for elem in root:
            tag = elem.tag.lower()
            if "httpsample" in tag or "sample" in tag:
                elapsed = int(elem.get("t", elem.get("lt", 0)))
                success = elem.get("s", "true").lower() == "true"
                ts = int(elem.get("ts", 0))
                bytes_val = int(elem.get("by", 0))
                samples.append({"elapsed": elapsed, "success": success, "timestamp": ts, "bytes": bytes_val})
    except ET.ParseError:
        pass
    return samples


def parse_jtl(jtl_path):
    """Parse JMeter JTL file (CSV or XML) and return sample list + stats."""
    samples = []
    if not os.path.exists(jtl_path) or os.path.getsize(jtl_path) == 0:
        return samples, {}

    with open(jtl_path, "rb") as f:
        head = f.read(100)
    is_xml = head.strip().startswith(b"<?xml") or head.strip().startswith(b"<testResults")
    samples = _parse_jtl_xml(jtl_path) if is_xml else _parse_jtl_csv(jtl_path)

    if not samples:
        return samples, {}

    elapsed_list = [s["elapsed"] for s in samples]
    success_list = [s["success"] for s in samples]
    bytes_list = [s["bytes"] for s in samples]
    ts_list = [s["timestamp"] for s in samples]

    total = len(samples)
    errors = total - sum(1 for s in success_list if s)
    success_count = total - errors

    # Duration
    duration_sec = 0
    if len(ts_list) >= 2:
        duration_sec = (max(ts_list) - min(ts_list)) / 1000.0

    # Throughput
    throughput = total / duration_sec if duration_sec > 0 else 0

    # Percentiles
    sorted_elapsed = sorted(elapsed_list)

    def percentile(arr, p):
        if not arr:
            return 0
        k = (len(arr) - 1) * p / 100
        f = int(k)
        c = min(f + 1, len(arr) - 1)
        return arr[f] + (k - f) * (arr[c] - arr[f]) if f != c else arr[f]

    p90 = percentile(sorted_elapsed, 90)
    p95 = percentile(sorted_elapsed, 95)
    p99 = percentile(sorted_elapsed, 99)

    # Response time buckets (ms)
    buckets = {"<200": 0, "200-500": 0, "500-1000": 0, ">1000": 0}
    for e in elapsed_list:
        if e < 200:
            buckets["<200"] += 1
        elif e < 500:
            buckets["200-500"] += 1
        elif e < 1000:
            buckets["500-1000"] += 1
        else:
            buckets[">1000"] += 1

    # Response time over time (for chart) - sample every N points
    step = max(1, len(ts_list) // 56)
    chart_data = [elapsed_list[i] for i in range(0, len(elapsed_list), step)][:56]

    stats = {
        "samples": total,
        "errors": errors,
        "success_count": success_count,
        "error_rate": (errors / total * 100) if total else 0,
        "success_rate": (success_count / total * 100) if total else 100,
        "min": min(elapsed_list),
        "max": max(elapsed_list),
        "avg": sum(elapsed_list) / total,
        "p90": p90,
        "p95": p95,
        "p99": p99,
        "duration_sec": duration_sec,
        "throughput": throughput,
        "bytes_total": sum(bytes_list),
        "buckets": buckets,
        "chart_data": chart_data,
        "is_complete": False,
    }
    return samples, stats


def generate_html(stats, output_path, endpoint="API Endpoint", test_date=None):
    """Generate stress-test-report.html from stats."""
    if test_date is None:
        test_date = datetime.now().strftime("%Y-%m-%d")

    err_rate = stats.get("error_rate", 0)
    success_rate = stats.get("success_rate", 100)
    verdict = "FAILED" if err_rate > 5 else ("WARNING" if err_rate > 0 else "PASSED")
    verdict_emoji = "❌" if err_rate > 5 else ("⚠️" if err_rate > 0 else "✅")
    badge_text = "Critical" if err_rate > 5 else ("Warning" if err_rate > 0 else "Test Completed Successfully")
    badge_class = "fail" if err_rate > 5 else ("warn" if err_rate > 0 else "pass")

    buckets = stats.get("buckets", {"<200": 0, "200-500": 0, "500-1000": 0, ">1000": 0})
    total = stats.get("samples", 1)
    b_max = max(buckets.values()) or 1
    bar_widths = {k: (v / b_max * 100) for k, v in buckets.items()}

    # Score 0-100 based on success rate and response time
    score = int(success_rate * 0.7 + min(30, (1000 - stats.get("avg", 0)) / 50) * 0.3)
    score = max(0, min(100, score))

    # Chart data JSON for JS
    chart_data_js = json.dumps(stats.get("chart_data", []))

    html_content = f'''<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>🔥 Stress Test Report - PharmaCare API</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet">
    <style>
        :root {{
            --bg-primary: #0a0e27; --bg-secondary: #111638; --bg-card: #161b40; --bg-card-hover: #1c2250;
            --accent-green: #00e676; --accent-green-soft: rgba(0, 230, 118, 0.15);
            --accent-blue: #448aff; --accent-blue-soft: rgba(68, 138, 255, 0.15);
            --accent-orange: #ff9100; --accent-orange-soft: rgba(255, 145, 0, 0.15);
            --accent-red: #ff5252; --accent-red-soft: rgba(255, 82, 82, 0.15);
            --accent-purple: #b388ff; --accent-purple-soft: rgba(179, 136, 255, 0.15);
            --text-primary: #e8eaed; --text-secondary: #9aa0b4; --text-muted: #5f6580;
            --border-color: rgba(255, 255, 255, 0.06);
        }}
        * {{ margin: 0; padding: 0; box-sizing: border-box; }}
        body {{ font-family: 'Inter', sans-serif; background: var(--bg-primary); color: var(--text-primary); min-height: 100vh; }}
        .container {{ max-width: 1400px; margin: 0 auto; padding: 40px 24px; }}
        .header {{ text-align: center; margin-bottom: 48px; }}
        .header-badge {{ display: inline-flex; align-items: center; gap: 8px; padding: 8px 20px; border-radius: 100px; font-size: 13px; font-weight: 600; margin-bottom: 20px; }}
        .header-badge.pass {{ background: var(--accent-green-soft); color: var(--accent-green); border: 1px solid rgba(0,230,118,0.25); }}
        .header-badge.warn {{ background: var(--accent-orange-soft); color: var(--accent-orange); border: 1px solid rgba(255,145,0,0.25); }}
        .header-badge.fail {{ background: var(--accent-red-soft); color: var(--accent-red); border: 1px solid rgba(255,82,82,0.25); }}
        .header h1 {{ font-size: 48px; font-weight: 900; background: linear-gradient(135deg, #fff 0%, #448aff 50%, #b388ff 100%); -webkit-background-clip: text; -webkit-text-fill-color: transparent; margin-bottom: 12px; }}
        .header p {{ color: var(--text-secondary); font-size: 18px; }}
        .endpoint-tag {{ display: inline-block; margin-top: 16px; background: var(--bg-card); border: 1px solid var(--border-color); padding: 10px 24px; border-radius: 12px; font-family: monospace; font-size: 14px; color: var(--accent-blue); }}
        .stats-grid {{ display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; margin-bottom: 36px; }}
        .stat-card {{ background: var(--bg-card); border: 1px solid var(--border-color); border-radius: 20px; padding: 28px; }}
        .stat-card.green .stat-value {{ color: var(--accent-green); }}
        .stat-card.blue .stat-value {{ color: var(--accent-blue); }}
        .stat-card.orange .stat-value {{ color: var(--accent-orange); }}
        .stat-card.purple .stat-value {{ color: var(--accent-purple); }}
        .stat-label {{ font-size: 12px; color: var(--text-muted); text-transform: uppercase; margin-bottom: 12px; }}
        .stat-value {{ font-size: 40px; font-weight: 800; }}
        .stat-sub {{ font-size: 13px; color: var(--text-secondary); margin-top: 8px; }}
        .chart-card {{ background: var(--bg-card); border: 1px solid var(--border-color); border-radius: 20px; padding: 28px; margin-bottom: 20px; }}
        .bar-chart {{ display: flex; flex-direction: column; gap: 14px; }}
        .bar-row {{ display: flex; align-items: center; gap: 12px; }}
        .bar-label {{ font-size: 13px; color: var(--text-secondary); min-width: 90px; text-align: right; }}
        .bar-track {{ flex: 1; height: 32px; background: rgba(255,255,255,0.03); border-radius: 8px; overflow: hidden; }}
        .bar-fill {{ height: 100%; border-radius: 8px; display: flex; align-items: center; padding: 0 12px; font-size: 12px; font-weight: 700; transition: width 1.5s; }}
        .bar-fill.fast {{ background: linear-gradient(90deg, #00e676, #69f0ae); }}
        .bar-fill.medium {{ background: linear-gradient(90deg, #448aff, #82b1ff); }}
        .bar-fill.slow {{ background: linear-gradient(90deg, #ff9100, #ffab40); }}
        .bar-fill.very-slow {{ background: linear-gradient(90deg, #ff5252, #ff8a80); }}
        .details-grid {{ display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }}
        .detail-item {{ background: var(--bg-card); border: 1px solid var(--border-color); border-radius: 16px; padding: 20px; }}
        .detail-item .label {{ font-size: 12px; color: var(--text-muted); text-transform: uppercase; margin-bottom: 8px; }}
        .detail-item .value {{ font-size: 20px; font-weight: 700; }}
        .verdict-card {{ background: linear-gradient(135deg, rgba(0,230,118,0.08), rgba(68,138,255,0.08)); border: 1px solid rgba(0,230,118,0.2); border-radius: 24px; padding: 40px; text-align: center; margin: 36px 0; }}
        .verdict-emoji {{ font-size: 64px; margin-bottom: 16px; }}
        .verdict-title {{ font-size: 32px; font-weight: 800; margin-bottom: 12px; }}
        .footer {{ text-align: center; padding: 40px 0; color: var(--text-muted); font-size: 13px; }}
        .live-badge {{ display: inline-block; background: #ff5252; color: white; padding: 4px 12px; border-radius: 100px; font-size: 11px; font-weight: 700; animation: pulse 2s infinite; margin-left: 8px; }}
        @keyframes pulse {{ 0%,100% {{ opacity: 1; }} 50% {{ opacity: 0.6; }} }}
        @media (max-width: 1024px) {{ .stats-grid {{ grid-template-columns: repeat(2, 1fr); }} .details-grid {{ grid-template-columns: repeat(2, 1fr); }} }}
        @media (max-width: 640px) {{ .stats-grid {{ grid-template-columns: 1fr; }} .header h1 {{ font-size: 32px; }} }}
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <div class="header-badge {badge_class}">
                {verdict_emoji} {badge_text}
            </div>
            <h1>🔥 Stress Test Report</h1>
            <p>PharmaCare Medicine Management System — Load Testing Results</p>
            <div class="endpoint-tag">{endpoint}</div>
        </div>

        <div class="stats-grid">
            <div class="stat-card green">
                <div class="stat-label">Total Requests</div>
                <div class="stat-value">{stats.get('samples', 0):,}</div>
                <div class="stat-sub">Samples recorded</div>
            </div>
            <div class="stat-card blue">
                <div class="stat-label">Success Rate</div>
                <div class="stat-value">{success_rate:.1f}%</div>
                <div class="stat-sub">{stats.get('errors', 0):,} errors / {stats.get('success_count', 0):,} success</div>
            </div>
            <div class="stat-card orange">
                <div class="stat-label">Duration</div>
                <div class="stat-value">~{int(stats.get('duration_sec', 0))}s</div>
                <div class="stat-sub">Total test execution time</div>
            </div>
            <div class="stat-card purple">
                <div class="stat-label">Throughput</div>
                <div class="stat-value">~{stats.get('throughput', 0):.1f}</div>
                <div class="stat-sub">Requests per second</div>
            </div>
        </div>

        <div class="chart-card">
            <h3>📊 Response Time Distribution</h3>
            <div class="bar-chart">
                <div class="bar-row">
                    <span class="bar-label">&lt; 200ms</span>
                    <div class="bar-track">
                        <div class="bar-fill fast" style="width:{bar_widths.get('<200',0)}%">{buckets.get('<200',0)} req</div>
                    </div>
                </div>
                <div class="bar-row">
                    <span class="bar-label">200-500ms</span>
                    <div class="bar-track">
                        <div class="bar-fill medium" style="width:{bar_widths.get('200-500',0)}%">{buckets.get('200-500',0)} req</div>
                    </div>
                </div>
                <div class="bar-row">
                    <span class="bar-label">500ms-1s</span>
                    <div class="bar-track">
                        <div class="bar-fill slow" style="width:{bar_widths.get('500-1000',0)}%">{buckets.get('500-1000',0)} req</div>
                    </div>
                </div>
                <div class="bar-row">
                    <span class="bar-label">&gt; 1s</span>
                    <div class="bar-track">
                        <div class="bar-fill very-slow" style="width:{bar_widths.get('>1000',0)}%">{buckets.get('>1000',0)} req</div>
                    </div>
                </div>
            </div>
            <div style="margin-top: 28px;">
                <h3>📈 Response Time Over Test Duration</h3>
                <canvas id="responseTimeChart" width="800" height="300"></canvas>
            </div>
        </div>

        <div class="chart-card">
            <h3>📋 Chi Tiết Kết Quả</h3>
            <div class="details-grid">
                <div class="detail-item"><div class="label">Min Response Time</div><div class="value" style="color:var(--accent-green)">{stats.get('min',0):,}ms</div></div>
                <div class="detail-item"><div class="label">Avg Response Time</div><div class="value" style="color:var(--accent-blue)">{stats.get('avg',0):,.0f}ms</div></div>
                <div class="detail-item"><div class="label">Max Response Time</div><div class="value" style="color:var(--accent-orange)">{stats.get('max',0):,}ms</div></div>
                <div class="detail-item"><div class="label">90th Percentile</div><div class="value" style="color:var(--accent-blue)">{stats.get('p90',0):,.0f}ms</div></div>
                <div class="detail-item"><div class="label">95th Percentile</div><div class="value" style="color:var(--accent-orange)">{stats.get('p95',0):,.0f}ms</div></div>
                <div class="detail-item"><div class="label">99th Percentile</div><div class="value" style="color:var(--accent-red)">{stats.get('p99',0):,.0f}ms</div></div>
                <div class="detail-item"><div class="label">Error Rate</div><div class="value" style="color:{"var(--accent-red)" if err_rate > 0 else "var(--accent-green)"}">{err_rate:.2f}%</div></div>
                <div class="detail-item"><div class="label">Data Transferred</div><div class="value">~{stats.get('bytes_total',0)/1024/1024:.2f} MB</div></div>
                <div class="detail-item"><div class="label">Overall Score</div><div class="value" style="color:var(--accent-purple)">{score}/100</div></div>
            </div>
        </div>

        <div class="verdict-card">
            <div class="verdict-emoji">{verdict_emoji}</div>
            <div class="verdict-title">STRESS TEST: {verdict}</div>
            <div class="verdict-text">
                {stats.get('samples',0):,} requests đã được thực hiện. {stats.get('success_count',0):,} thành công, {stats.get('errors',0):,} lỗi. 
                Error rate: {err_rate:.2f}%. Throughput: {stats.get('throughput',0):.1f} req/s. 
                Avg response time: {stats.get('avg',0):,.0f}ms.
            </div>
        </div>

        <div class="chart-card">
            <h3>⚙️ Cấu Hình Test</h3>
            <div class="details-grid">
                <div class="detail-item"><div class="label">Tool</div><div class="value" style="font-size:16px">Apache JMeter 5.6.3</div></div>
                <div class="detail-item"><div class="label">Test Date</div><div class="value" style="font-size:16px">{test_date}</div></div>
                <div class="detail-item"><div class="label">Report Generated</div><div class="value" style="font-size:14px">{datetime.now().strftime("%Y-%m-%d %H:%M:%S")}</div></div>
            </div>
        </div>

        <div class="footer">
            <p>Generated by JMeter Reporter • {test_date}</p>
            <p>LAB211 - Group 1 Project</p>
        </div>
    </div>

    <script>
        const chartData = {chart_data_js};
        const canvas = document.getElementById('responseTimeChart');
        if (canvas && chartData.length > 0) {{
            const ctx = canvas.getContext('2d');
            const w = canvas.width; const h = canvas.height;
            const padding = {{ top: 20, right: 20, bottom: 40, left: 50 }};
            const chartW = w - padding.left - padding.right;
            const chartH = h - padding.top - padding.bottom;
            const maxVal = Math.max(...chartData) * 1.2 || 1;

            // Clear
            ctx.fillStyle = '#0a0e27';
            ctx.fillRect(0, 0, w, h);

            // Grid
            ctx.strokeStyle = 'rgba(255,255,255,0.05)';
            for (let i = 0; i <= 5; i++) {{
                const y = padding.top + (chartH / 5) * i;
                ctx.beginPath();
                ctx.moveTo(padding.left, y);
                ctx.lineTo(w - padding.right, y);
                ctx.stroke();
            }}

            // Line
            const gradient = ctx.createLinearGradient(0, padding.top, 0, h);
            gradient.addColorStop(0, 'rgba(68,138,255,0.3)');
            gradient.addColorStop(1, 'rgba(68,138,255,0)');
            ctx.beginPath();
            chartData.forEach((val, i) => {{
                const x = padding.left + (chartW / (chartData.length - 1 || 1)) * i;
                const y = padding.top + chartH - (val / maxVal) * chartH;
                if (i === 0) ctx.moveTo(x, y);
                else ctx.lineTo(x, y);
            }});
            ctx.lineTo(padding.left + chartW, h - padding.bottom);
            ctx.closePath();
            ctx.fillStyle = gradient;
            ctx.fill();
            ctx.beginPath();
            chartData.forEach((val, i) => {{
                const x = padding.left + (chartW / (chartData.length - 1 || 1)) * i;
                const y = padding.top + chartH - (val / maxVal) * chartH;
                if (i === 0) ctx.moveTo(x, y);
                else ctx.lineTo(x, y);
            }});
            ctx.strokeStyle = '#448aff';
            ctx.lineWidth = 2.5;
            ctx.stroke();
        }}
    </script>
</body>
</html>
'''
    with open(output_path, "w", encoding="utf-8") as f:
        f.write(html_content)
    print(f"  [OK] Report written to {output_path}")


def watch_and_report(config):
    """Watch JTL file and update report in real-time until test completes."""
    project_dir = SCRIPT_DIR
    jtl_path = project_dir / config["paths"]["resultsJtl"]
    html_path = project_dir / config["paths"]["outputHtml"]
    idle_sec = config["watch"]["idleSecondsBeforeComplete"]
    poll_ms = config["watch"]["pollIntervalMs"]
    poll_sec = poll_ms / 1000

    print("=" * 60)
    print("JMeter Stress Test Reporter - Real-time Mode")
    print("=" * 60)
    print(f"Monitoring: {jtl_path}")
    print(f"Output:     {html_path}")
    print(f"Complete when file idle for: {idle_sec}s")
    print("-" * 60)
    print("Run stress test in JMeter (GUI or CLI).")
    print("GUI: Add Simple Data Writer, set filename to:", jtl_path)
    print("CLI: jmeter -n -t <test.jmx> -l", jtl_path.name)
    print("-" * 60)

    last_size = 0
    last_update = time.time()
    last_stats = {}

    while True:
        try:
            if jtl_path.exists():
                size = os.path.getsize(jtl_path)
                if size > 0:
                    samples, stats = parse_jtl(str(jtl_path))
                    if samples:
                        last_size = size
                        last_update = time.time()
                        last_stats = stats
                        # Extract endpoint from first sample if available
                        generate_html(stats, str(html_path))
                        print(f"  [{datetime.now().strftime('%H:%M:%S')}] {stats['samples']:,} samples | "
                              f"Error: {stats['error_rate']:.1f}% | "
                              f"Avg: {stats['avg']:,.0f}ms | "
                              f"Throughput: {stats['throughput']:.1f}/s")
                else:
                    if last_size > 0 and (time.time() - last_update) > idle_sec:
                        print("\n[OK] Test complete. Final report saved.")
                        break
            else:
                if last_size > 0 and (time.time() - last_update) > idle_sec:
                    print("\n[OK] Test complete. Final report saved.")
                    break
                print("  Waiting for results file...", end="\r")

            time.sleep(poll_sec)
        except KeyboardInterrupt:
            print("\n\nStopped by user.")
            if last_stats:
                generate_html(last_stats, str(html_path))
            break


def run_jmeter_and_watch(config):
    """Run JMeter in CLI mode and watch results."""
    jmeter_home = Path(config["jmeter"]["home"])
    jmx_path = Path(config["jmeter"]["jmx"])
    project_dir = SCRIPT_DIR
    jtl_path = project_dir / config["paths"]["resultsJtl"]

    jmeter_bat = jmeter_home / "bin" / "jmeter.bat"
    if not jmeter_bat.exists():
        print(f"ERROR: JMeter not found at {jmeter_bat}")
        sys.exit(1)
    if not jmx_path.exists():
        print(f"ERROR: JMX file not found: {jmx_path}")
        sys.exit(1)

    # Remove old results
    if jtl_path.exists():
        jtl_path.unlink()

    print("=" * 60)
    print("JMeter Stress Test Reporter - Run & Watch Mode")
    print("=" * 60)
    print(f"JMeter: {jmeter_bat}")
    print(f"Test:   {jmx_path}")
    print(f"Output: {jtl_path}")
    print("-" * 60)
    print("Starting JMeter in non-GUI mode...")
    print("-" * 60)

    cmd = [
        str(jmeter_bat),
        "-n",
        "-t", str(jmx_path),
        "-l", str(jtl_path),
        "-j", str(project_dir / "jmeter-reporter.log"),
    ]
    proc = subprocess.Popen(cmd, cwd=str(jmeter_home / "bin"), shell=True)
    time.sleep(2)

    watch_and_report(config)
    proc.wait()


def main():
    parser = argparse.ArgumentParser(description="JMeter Stress Test Reporter")
    parser.add_argument("--run", action="store_true", help="Chạy JMeter trong chế độ CLI và theo dõi")
    parser.add_argument("--watch", action="store_true", help="Chỉ theo dõi file JTL (khi chạy JMeter GUI)")
    args = parser.parse_args()

    config = load_config()

    if args.run:
        run_jmeter_and_watch(config)
    else:
        watch_and_report(config)


if __name__ == "__main__":
    main()
