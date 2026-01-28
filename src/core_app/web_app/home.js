// Github Pharmacy - Real Data Sync + Mock Pricing
// Chế độ: Đọc trực tiếp từ /data/medicines_clean.csv (Tương thích Live Server)

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
  return n.toLocaleString("vi-VN") + "đ";
}

function clampNumber(val) {
  if (val === "" || val === null || val === undefined) return null;
  const num = Number(val);
  return Number.isFinite(num) ? num : null;
}

function hashString(s) {
  let h = 0;
  for (let i = 0; i < s.length; i++) {
    h = (h * 31 + s.charCodeAt(i)) >>> 0;
  }
  return h;
}

// -------- CSV Parser + Mock Pricing --------
function parseCSV(text) {
  const lines = text.split("\n");
  const rows = [];

  lines.forEach((line, index) => {
    const trimmed = line.trim();
    if (!trimmed || trimmed.startsWith("medicine_id")) return;

    const parts = trimmed.split(",");
    if (parts.length < 5) return;

    const [medId, name, batchId, dateStr, quantityStr] = parts;
    const quantity = Number(quantityStr);

    if (!medId || !name) return;

    // --- Mocking Pricing & Sale (Deterministic based on medId) ---
    const h = hashString(medId);
    const price = 10000 + (h % 200) * 1000; // 10k - 210k
    const storeIdx = (h % 5) + 1;
    const store = `CN${storeIdx}`;

    const hasSale = h % 5 === 0; // 20% chance
    const discount = hasSale ? (5 + (hashString(name) % 26)) : 0;
    const finalPrice = discount ? Math.round(price * (1 - discount / 100)) : price;
    const popularity = (hashString(name + batchId) % 1000) + 1;

    rows.push({
      id: medId,
      name: name.replaceAll("_", " "),
      batchId,
      date: dateStr,
      quantity: isNaN(quantity) ? 0 : quantity,
      price,
      discount,
      finalPrice,
      store,
      popularity
    });
  });

  return rows;
}

// -------- CSV Loader --------
async function loadProducts() {
  try {
    // Relative path used because Live Server root varies, but usually it's project root
    // Try absolute from root first
    const res = await fetch("/data/medicines_clean.csv", { cache: "no-store" });
    if (!res.ok) {
      // Fallback for different Live Server configurations
      const fallbackRes = await fetch("../../../data/medicines_clean.csv", { cache: "no-store" });
      if (!fallbackRes.ok) throw new Error("CSV Not Found");
      const text = await fallbackRes.text();
      return parseCSV(text);
    }
    const text = await res.text();
    return parseCSV(text);
  } catch (e) {
    console.error("Failed to load medicines:", e);
    return [];
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
            <div class="line">Mã: <b>${escapeHtml(p.id)}</b> • Lô: <b>${escapeHtml(p.batchId)}</b></div>
            <div class="line">CN: <b>${escapeHtml(p.store)}</b> • HSD: <b>${escapeHtml(p.date)}</b></div>
          </div>
        </div>
        ${saleTag}
      </div>

      <div class="card__mid">
        ${priceHtml}
        <div class="unit-selector">
          <select class="field-select unit-input" data-id="${p.id}">
            <option value="Viên">Viên</option>
            <option value="Vỉ">Vỉ</option>
            <option value="Hộp">Hộp</option>
          </select>
        </div>
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
  const uniqueSale = filtered.filter(p => p.discount > 0).slice(0, 8);
  $("saleGrid").innerHTML = uniqueSale.map(productCard).join("");
  $("saleEmpty").classList.toggle("hidden", uniqueSale.length > 0);
}

function renderBest(filtered) {
  const uniqueBest = [...filtered].sort((a, b) => b.popularity - a.popularity).slice(0, 8);
  $("bestGrid").innerHTML = uniqueBest.map(productCard).join("");
  $("bestEmpty").classList.toggle("hidden", uniqueBest.length > 0);
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
  const seen = new Set();
  const uniqueProducts = state.products.filter(p => {
    if (seen.has(p.id)) return false;
    seen.add(p.id);
    return true;
  });

  const filtered = applyFilters(uniqueProducts);
  renderSale(filtered);
  renderBest(filtered);
  renderAll(filtered);
}

// -------- Events --------
function bindEvents() {
  const getCart = () => JSON.parse(localStorage.getItem("cart") || "[]");
  const saveCart = (cart) => {
    localStorage.setItem("cart", JSON.stringify(cart));
    $("cartBadge").textContent = String(cart.reduce((sum, item) => sum + item.qty, 0));
  };

  // Set initial badge
  saveCart(getCart());

  document.body.addEventListener("click", (e) => {
    const buyId = e.target?.getAttribute?.("data-buy");
    const addId = e.target?.getAttribute?.("data-add");

    if (buyId || addId) {
      const id = buyId || addId;
      const product = state.products.find((p) => p.id === id);
      const unit = document.querySelector(`.unit-input[data-id="${id}"]`)?.value || "Viên";

      let cart = getCart();
      const existing = cart.find(item => item.id === id && item.unit === unit);

      if (existing) {
        existing.qty += 1;
      } else {
        cart.push({
          id: product.id,
          name: product.name,
          price: product.finalPrice,
          unit: unit,
          qty: 1
        });
      }

      saveCart(cart);
      showToast(product.name);

      if (buyId) {
        window.location.href = "cart.html";
      }
      return;
    }
  });

  $("btnCart").addEventListener("click", () => {
    window.location.href = "cart.html";
  });

  function showToast(msg) {
    let t = $("toast");
    if (!t) {
      t = document.createElement("div");
      t.id = "toast";
      t.className = "toast";
      document.body.appendChild(t);
    }
    t.innerHTML = `🛒 <span>${msg}</span> đã được thêm vào giỏ!`;
    t.classList.add("show");
    setTimeout(() => t.classList.remove("show"), 3000);
  }

  $("btnSearch").addEventListener("click", () => {
    state.query = $("globalSearch").value;
    state.page = 1;
    renderAllSections();
    scrollToAll();
  });

  $("globalSearch").addEventListener("keydown", (e) => {
    if (e.key === "Enter") $("btnSearch").click();
  });

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

  $("btnGoAll").addEventListener("click", () => scrollToAll());

  // Auth logic
  const authState = window.authState || { isLoggedIn: false };
  const btnLogin = $("btnLogin");
  if (authState.isLoggedIn) {
    if (authState.role === "ADMIN") {
      const adminLink = document.createElement("a");
      adminLink.href = "admin/users";
      adminLink.className = "btn btn--ghost";
      adminLink.innerHTML = "⚙️ Admin";
      adminLink.style.textDecoration = "none";
      btnLogin.parentNode.insertBefore(adminLink, btnLogin);
    }
    btnLogin.innerHTML = `👤 ${escapeHtml(authState.fullName)} (Đăng xuất)`;
    btnLogin.addEventListener("click", (e) => {
      e.preventDefault();
      window.location.href = "logout";
    });
  } else {
    btnLogin.innerHTML = `👤 Đăng nhập`;
    btnLogin.addEventListener("click", (e) => {
      // Removed e.preventDefault() here so if it's an <a> tag, it can also work naturally
      window.location.href = "login.html";
    });
  }

  $("btnCart").addEventListener("click", () => {
    alert(`Giỏ hàng: ${state.cartCount} sản phẩm.`);
  });
}

function syncFiltersFromUI() {
  state.query = $("filterQuery").value || "";
  state.branch = $("filterBranch").value || "";
  state.min = $("filterMin").value || "";
  state.max = $("filterMax").value || "";
  state.sort = $("filterSort").value || "pop_desc";
  state.onlySale = $("filterOnlySale").checked;
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

function fillBranches(products) {
  const branches = [...new Set(products.map(p => p.store))].sort();
  const sel = $("filterBranch");
  sel.innerHTML = '<option value="">Tất cả</option>';
  for (const b of branches) {
    const opt = document.createElement("option");
    opt.value = b;
    opt.textContent = b;
    sel.appendChild(opt);
  }
}

// -------- Boot --------
(async function init() {
  try {
    const authRes = await fetch("api/auth-status");
    if (authRes.ok) window.authState = await authRes.json();
  } catch (e) { }

  state.products = await loadProducts();
  fillBranches(state.products);
  bindEvents();
  renderAllSections();
})();
