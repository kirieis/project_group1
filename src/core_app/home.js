// Github Pharmacy - Demo Frontend (HTML/CSS/JS)
// Hỗ trợ: search + filter + sort + pagination, phù hợp 10k sản phẩm (render theo trang)

const state = {
  products: [],
  cartCount: 0,

  // filters
  query: "",
  branch: "",
  min: "",
  max: "",
  sort: "pop_desc",
  onlySale: false,

  // paging
  page: 1,
  pageSize: 12,
};

const $ = (id) => document.getElementById(id);

function formatVND(n) {
  // n là number
  return n.toLocaleString("vi-VN") + "đ";
}

function clampNumber(val) {
  const num = Number(val);
  return Number.isFinite(num) ? num : null;
}

// -------- Mock dataset builder (khi bạn chưa nối DB) --------
// Format legacy_batches.csv: batch_id,medicine_id,medicine_name,expiry_date,quantity_vien,branch_id
function mockFromCSVLines(lines) {
  const rows = [];
  for (const line of lines) {
    const trimmed = line.trim();
    if (!trimmed) continue;
    // Skip header row
    if (trimmed.startsWith("batch_id")) continue;

    const parts = trimmed.split(",");
    if (parts.length < 6) continue;

    const [batchId, medId, name, dateStr, quantityStr, store] = parts;

    // Skip invalid rows (empty medId or invalid data)
    if (!medId || !batchId || dateStr === "INVALID_DATE") continue;
    const quantity = Number(quantityStr);
    if (quantity < 0) continue; // Skip negative quantity

    // Mock price based on medicine ID (since CSV doesn't have price)
    const price = 10000 + (hashString(medId) % 200) * 1000; // 10,000 - 210,000 VND

    // mock: 20% sản phẩm có sale
    const hasSale = hashString(medId) % 5 === 0;
    const discount = hasSale ? (5 + (hashString(name) % 26)) : 0; // 5..30%
    const finalPrice = discount ? Math.round(price * (1 - discount / 100)) : price;

    // mock popularity (để tạo best seller)
    const popularity = (hashString(name + store) % 1000) + 1;

    rows.push({
      id: medId,
      batchId,
      name,
      date: dateStr,
      price,
      discount,
      finalPrice,
      store,
      quantity,
      popularity,
    });
  }
  return rows;
}

function hashString(s) {
  // hash nhanh để mock ngẫu nhiên ổn định
  let h = 0;
  for (let i = 0; i < s.length; i++) {
    h = (h * 31 + s.charCodeAt(i)) >>> 0;
  }
  return h;
}

// -------- CSV Loader --------
// Load từ file legacy_batches.csv trong thư mục data
async function loadProducts() {
  try {
    const res = await fetch("../data/legacy_batches.csv", { cache: "no-store" });
    if (!res.ok) throw new Error("No CSV");
    const text = await res.text();
    const lines = text.split("\n");
    return mockFromCSVLines(lines);
  } catch (e) {
    console.error("Failed to load CSV:", e);
    // fallback: demo vài dòng với format mới
    const sample = [
      "B1,M2,Thuoc_M2,2025-03-27,1400,CN5",
      "B2,M35,Thuoc_M35,2025-08-05,1300,CN5",
      "B3,M99,Vitamin_C_500mg,2025-01-12,500,CN1",
      "B4,M120,Paracetamol_500mg,2025-02-02,800,CN2",
      "B5,M77,Collagen_Beauty,2025-04-18,600,CN3",
      "B6,M18,Omega_3,2025-05-22,700,CN2",
    ];
    return mockFromCSVLines(sample);
  }
}

// -------- Filtering / Sorting / Paging --------
function applyFilters(products) {
  const q = state.query.trim().toLowerCase();
  const min = clampNumber(state.min);
  const max = clampNumber(state.max);

  let out = products;

  if (q) {
    out = out.filter(p => {
      const hay = `${p.name} ${p.id} ${p.batchId} ${p.store}`.toLowerCase();
      return hay.includes(q);
    });
  }

  if (state.branch) out = out.filter(p => p.store === state.branch);
  if (min !== null) out = out.filter(p => p.finalPrice >= min);
  if (max !== null) out = out.filter(p => p.finalPrice <= max);
  if (state.onlySale) out = out.filter(p => p.discount > 0);

  out = sortProducts(out, state.sort);
  return out;
}

function sortProducts(arr, sortKey) {
  const a = [...arr];
  switch (sortKey) {
    case "price_asc":
      a.sort((x, y) => x.finalPrice - y.finalPrice);
      break;
    case "price_desc":
      a.sort((x, y) => y.finalPrice - x.finalPrice);
      break;
    case "date_desc":
      a.sort((x, y) => String(y.date).localeCompare(String(x.date)));
      break;
    case "name_asc":
      a.sort((x, y) => String(x.name).localeCompare(String(y.name), "vi"));
      break;
    case "pop_desc":
    default:
      a.sort((x, y) => y.popularity - x.popularity);
      break;
  }
  return a;
}

function paginate(arr) {
  const total = arr.length;
  const totalPages = Math.max(1, Math.ceil(total / state.pageSize));
  state.page = Math.min(state.page, totalPages);

  const start = (state.page - 1) * state.pageSize;
  const end = start + state.pageSize;
  return {
    items: arr.slice(start, end),
    total,
    totalPages,
  };
}

// -------- UI Rendering --------
function productCard(p) {
  const saleTag = p.discount > 0
    ? `<span class="tag tag--sale">SALE -${p.discount}%</span>`
    : `<span class="tag">NEW</span>`;

  const priceHtml = p.discount > 0
    ? `<span class="price">${formatVND(p.finalPrice)} <del>${formatVND(p.price)}</del></span>`
    : `<span class="price">${formatVND(p.finalPrice)}</span>`;

  return `
    <article class="card">
      <div class="card__top">
        <div>
          <h3 class="card__name">${escapeHtml(p.name)}</h3>
          <div class="card__meta">
            Mã: <b>${escapeHtml(p.id)}</b> • Lô: <b>${escapeHtml(p.batchId)}</b><br/>
            CN: <b>${escapeHtml(p.store)}</b> • Ngày: <b>${escapeHtml(p.date)}</b>
          </div>
        </div>
        ${saleTag}
      </div>

      <div class="card__mid">
        ${priceHtml}
        <span class="tag">★ ${p.popularity}</span>
      </div>

      <div class="card__actions">
        <button class="btn btn--buy" data-buy="${p.id}">MUA NGAY</button>
        <button class="btn btn--add" data-add="${p.id}">+ Giỏ</button>
      </div>
    </article>
  `;
}

function escapeHtml(str) {
  return String(str)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

function renderSale(filtered) {
  const sale = filtered.filter(p => p.discount > 0).slice(0, 8);
  $("saleGrid").innerHTML = sale.map(productCard).join("");
  $("saleEmpty").classList.toggle("hidden", sale.length > 0);
}

function renderBest(filtered) {
  // best seller = top popularity
  const best = [...filtered].sort((a, b) => b.popularity - a.popularity).slice(0, 8);
  $("bestGrid").innerHTML = best.map(productCard).join("");
  $("bestEmpty").classList.toggle("hidden", best.length > 0);
}

function renderAll(filtered) {
  const { items, total, totalPages } = paginate(filtered);

  $("resultCount").textContent = `${total.toLocaleString("vi-VN")} kết quả`;
  $("pageNow").textContent = String(state.page);
  $("pageTotal").textContent = String(totalPages);

  $("allGrid").innerHTML = items.map(productCard).join("");
  $("allEmpty").classList.toggle("hidden", total > 0);

  $("prevPage").disabled = state.page <= 1;
  $("nextPage").disabled = state.page >= totalPages;
}

function renderAllSections() {
  const filtered = applyFilters(state.products);
  renderSale(filtered);
  renderBest(filtered);
  renderAll(filtered);
}

// -------- Events --------
function bindEvents() {
  // Cart buttons (event delegation)
  document.body.addEventListener("click", (e) => {
    const buyId = e.target?.getAttribute?.("data-buy");
    const addId = e.target?.getAttribute?.("data-add");

    if (buyId || addId) {
      state.cartCount += 1;
      $("cartBadge").textContent = String(state.cartCount);
      return;
    }
  });

  // Header search -> sync filterQuery + apply
  $("btnSearch").addEventListener("click", () => {
    $("filterQuery").value = $("globalSearch").value;
    state.query = $("globalSearch").value;
    state.page = 1;
    renderAllSections();
    scrollToAll();
  });

  $("globalSearch").addEventListener("keydown", (e) => {
    if (e.key === "Enter") $("btnSearch").click();
  });

  // Apply + Reset
  $("btnApply").addEventListener("click", () => {
    syncFiltersFromUI();
    state.page = 1;
    renderAllSections();
  });

  $("btnReset").addEventListener("click", () => {
    resetFiltersUI();
    syncFiltersFromUI();
    state.page = 1;
    renderAllSections();
  });

  // Pagination
  $("prevPage").addEventListener("click", () => {
    state.page = Math.max(1, state.page - 1);
    renderAllSections();
    scrollToAll();
  });

  $("nextPage").addEventListener("click", () => {
    state.page += 1;
    renderAllSections();
    scrollToAll();
  });

  // Debounce typing for filterQuery (đỡ giật)
  const debounced = debounce(() => {
    syncFiltersFromUI();
    state.page = 1;
    renderAllSections();
  }, 250);

  ["filterQuery", "filterMin", "filterMax"].forEach(id => {
    $(id).addEventListener("input", debounced);
  });

  ["filterBranch", "filterSort", "filterOnlySale"].forEach(id => {
    $(id).addEventListener("change", () => {
      syncFiltersFromUI();
      state.page = 1;
      renderAllSections();
    });
  });

  // CTA
  $("btnGoSale").addEventListener("click", () => {
    document.getElementById("saleSection").scrollIntoView({ behavior: "smooth" });
  });
  $("btnGoAll").addEventListener("click", () => scrollToAll());

  // Login mock
  $("btnLogin").addEventListener("click", () => {
    alert("Demo: Màn đăng nhập bạn tự làm thêm (modal/route).");
  });

  $("btnCart").addEventListener("click", () => {
    alert(`Giỏ hàng demo: ${state.cartCount} sản phẩm (bạn tự nối DB/cart sau).`);
  });
}

function scrollToAll() {
  document.getElementById("allSection").scrollIntoView({ behavior: "smooth" });
}

function debounce(fn, ms) {
  let t = null;
  return (...args) => {
    clearTimeout(t);
    t = setTimeout(() => fn(...args), ms);
  };
}

function syncFiltersFromUI() {
  state.query = $("filterQuery").value || "";
  state.branch = $("filterBranch").value || "";
  state.min = $("filterMin").value || "";
  state.max = $("filterMax").value || "";
  state.sort = $("filterSort").value || "pop_desc";
  state.onlySale = $("filterOnlySale").checked;

  // sync header search (cho đồng bộ)
  $("globalSearch").value = state.query;
}

function resetFiltersUI() {
  $("filterQuery").value = "";
  $("filterBranch").value = "";
  $("filterMin").value = "";
  $("filterMax").value = "";
  $("filterSort").value = "pop_desc";
  $("filterOnlySale").checked = false;
  $("globalSearch").value = "";
}

function fillBranches(products) {
  const branches = [...new Set(products.map(p => p.store))].sort((a, b) => a.localeCompare(b, "vi"));
  const sel = $("filterBranch");
  for (const b of branches) {
    const opt = document.createElement("option");
    opt.value = b;
    opt.textContent = b;
    sel.appendChild(opt);
  }
}

// -------- Boot --------
(async function init() {
  state.products = await loadProducts();
  fillBranches(state.products);
  bindEvents();
  renderAllSections();
})();
