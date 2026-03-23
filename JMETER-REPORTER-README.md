# JMeter Stress Test Reporter - Hướng Dẫn Kết Nối

Script `jmeter-reporter.py` kết nối với Apache JMeter 5.6.3 để **ghi nhận real-time** kết quả stress test và **tự động tạo** file `stress-test-report.html` khi bạn dừng test.

## Cách Sử Dụng

### Cách 1: Chạy JMeter từ script (CLI mode) – Đơn giản nhất

Script sẽ tự chạy JMeter ở chế độ non-GUI và theo dõi kết quả:

```powershell
cd E:\Project-LAB-github\lab211-project-group1
python jmeter-reporter.py --run
```

- JMeter chạy tự động
- Kết quả được ghi vào `stress-test-results.jtl`
- Báo cáo `stress-test-report.html` cập nhật real-time
- Khi test xong, báo cáo cuối cùng được lưu tự động

### Cách 2: Chạy JMeter GUI + theo dõi

Nếu bạn muốn dùng JMeter GUI (giao diện đồ họa):

#### Bước 1: Thêm Simple Data Writer vào Test Plan

1. Mở JMeter, load file test của bạn (vd: `unplanked-inculpably-malorie.ngrok-free.dev.jmx`)
2. Chuột phải vào **Test Plan** hoặc **Thread Group** → **Add** → **Listener** → **Simple Data Writer**
3. Trong Simple Data Writer:
   - Nhấn **Browse** (hoặc Configure)
   - Chọn đường dẫn: `E:\Project-LAB-github\lab211-project-group1\stress-test-results.jtl`
   - **Lưu** Test Plan (Ctrl+S)

#### Bước 2: Chạy Reporter (trước khi Start test)

Mở terminal mới trong thư mục project:

```powershell
cd E:\Project-LAB-github\lab211-project-group1
python jmeter-reporter.py --watch
```

#### Bước 3: Chạy stress test trong JMeter

1. Trong JMeter: nhấn **Start** (Ctrl+R)
2. Reporter sẽ cập nhật `stress-test-report.html` real-time
3. Khi bạn nhấn **Stop**, chờ ~5 giây → báo cáo cuối cùng được lưu

---

## Cấu Hình

Chỉnh sửa `jmeter-reporter.config.json` nếu cần:

```json
{
  "jmeter": {
    "home": "E:\\Project-LAB-github\\apache-jmeter-5.6.3\\apache-jmeter-5.6.3",
    "jmx": "đường_dẫn\\đến\\file.jmx"
  },
  "paths": {
    "resultsJtl": "stress-test-results.jtl",
    "outputHtml": "stress-test-report.html"
  }
}
```

## Yêu Cầu

- Python 3.x (có sẵn trên Windows)
- Apache JMeter 5.6.3 tại đường dẫn đã cấu hình

## Lưu Ý

- File `stress-test-results.jtl` có thể rất lớn khi chạy test dài → đã được thêm vào `.gitignore`
- Mở `stress-test-report.html` bằng trình duyệt để xem báo cáo
