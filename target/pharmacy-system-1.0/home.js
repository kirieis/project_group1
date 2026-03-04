// Github Pharmacy - Real Data Sync + Mock Pricing
// Chế độ: Đọc trực tiếp từ /data/medicines_clean.csv (Tương thích Live Server)

const state = {
  products: [],
  cartCount: 0,
  lang: localStorage.getItem("lang") || "vi", // Default to VI

  // filters
  query: "",
  min: "",
  max: "",
  sort: "pop_desc",
  onlySale: false,

  // paging
  page: 1,
  pageSize: 12,
};

// Removed local i18n object - now using global i18n.js
const txt = (key) => {
  const dotIndex = key.indexOf('.');
  if (dotIndex > -1) {
    return window.t ? window.t(key.substring(0, dotIndex), key.substring(dotIndex + 1)) : key;
  }
  // Fallback order: home -> common
  return window.t ? window.t('home', key) : key;
};

// -------- Localization --------
function updateLocale() {
  if (typeof translateAll === 'function') {
    translateAll();
  }

  const langBtn = $("btnLang");
  if (langBtn) {
    langBtn.innerHTML = currentLang === "vi" ? "VIE" : "ENG";
    langBtn.title = currentLang === "vi" ? "Tiếng Việt" : "English";
  }
}

function switchLanguage() {
  const newLang = currentLang === "vi" ? "en" : "vi";
  setLanguage(newLang);
  // Re-render everything that uses dynamic strings
  renderAllSections();
}

// Listen for global language changes
window.addEventListener('langChanged', (e) => {
  state.lang = e.detail;
  updateLocale();
});

const $ = (id) => document.getElementById(id);

function formatVND(n) {
  return n.toLocaleString("vi-VN") + "đ";
}

// Global Helpers
const getCart = () => JSON.parse(localStorage.getItem("cart") || "[]");
const saveCart = (cart) => {
  localStorage.setItem("cart", JSON.stringify(cart));
  const badge = $("cartBadge");
  if (badge) {
    badge.textContent = String(cart.length);
  }
};

function showToast(msg) {
  let t = $("toast");
  if (!t) {
    t = document.createElement("div");
    t.id = "toast";
    t.className = "toast";
    document.body.appendChild(t);
  }
  t.innerHTML = `🛒 <span>${msg}</span> ${txt('toast_added')}`;
  t.classList.add("show");
  setTimeout(() => t.classList.remove("show"), 3000);
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
  if (!text) return [];
  // Split by any line ending (CR, LF, CRLF)
  const lines = text.split(/\r?\n/);
  const rows = [];

  console.log(`[home.js] Parsing CSV: ${lines.length} lines detected.`);

  lines.forEach((line, index) => {
    const trimmed = line.trim();
    if (!trimmed) return;

    // Header check (case insensitive, skip if it smells like a header)
    const lowLine = trimmed.toLowerCase();
    if (lowLine.includes("medicine_id") || lowLine.includes("name") || lowLine.includes("quantity")) return;

    // Split by comma, but could be extended to ; or \t
    let parts = trimmed.split(",");
    if (parts.length < 5) parts = trimmed.split(";");
    if (parts.length < 5) parts = trimmed.split("\t");

    // We need at least 5 parts to show something (id, name, ...price)
    if (parts.length < 5) return;

    try {
      // medicine_id,name,batch,ingredient,dosage_form,strength,unit,manufacturer,expiry,quantity,price
      // 0:id, 1:name, 2:batch, 3:ingred, 4:form, 5:strength, 6:unit, 7:manuf, 8:expiry, 9:qty, 10:price
      const medId = (parts[0] || "").trim().replace(/^["']|["']$/g, '');
      const rawName = (parts[1] || "").trim().replace(/^["']|["']$/g, '');
      const batchId = (parts[2] || "").trim();
      const dosageForm = (parts[4] || "Tablet").trim();
      const dateStr = (parts[8] || "").trim();
      const quantityStr = (parts[9] || "0").trim();
      const priceStr = (parts[10] || "0").trim();

      if (!medId || !rawName) return;

      const name = rawName.split("_").join(" ");
      const quantity = parseInt(quantityStr) || 0;
      const price = parseInt(priceStr) || 0;

      // --- Sale Logic ---
      let hasSale = false;
      let daysUntilExpiry = 3650;

      if (dateStr) {
        let d = new Date(dateStr);
        if (isNaN(d.getTime())) {
          const dp = dateStr.split('/');
          if (dp.length === 3) d = new Date(`${dp[2]}-${dp[1]}-${dp[0]}`);
        }
        if (!isNaN(d.getTime())) {
          const diff = d - new Date();
          daysUntilExpiry = Math.ceil(diff / (1000 * 60 * 60 * 24));
        }
      }

      if (daysUntilExpiry > 0 && daysUntilExpiry <= 180) {
        hasSale = true;
      }

      const discount = hasSale ? (5 + (hashString(name) % 16)) : 0;
      const finalBasePrice = discount ? Math.round(price * (1 - discount / 100)) : price;
      const popularity = (hashString(name + batchId) % 1000) + 1;
      const isLiquid = ['Syrup', 'Suspension', 'Chai', 'Dịch'].some(kw => dosageForm.includes(kw));

      rows.push({
        id: medId,
        name,
        batchId,
        date: dateStr,
        quantity,
        dosageForm,
        isLiquid,
        price,
        discount,
        finalBasePrice,
        vienPerVi: 10,
        viPerHop: 3,
        popularity
      });
    } catch (err) {
      console.warn(`[home.js] Skip line ${index} due to error:`, err);
    }
  });

  return rows;
}

// -------- CSV Loader --------
async function loadProducts(skipSkeleton = false) {
  try {
    console.log("[home.js] Fetching fresh product data from: data/medicines_clean.csv");
    const grid = $("allGrid");
    if (grid && !skipSkeleton) {
      grid.innerHTML = `<div class="skeleton-ring-container" style="grid-column: 1/-1; text-align: center; padding: 50px; color: #64748b;">
        <div class="skeleton-ring"></div>
        <p style="margin-top: 15px; font-weight: 600;" data-i18n="home.loading">Đang tải sản phẩm...</p>
      </div>`;
    }

    const res = await fetch("data/medicines_clean.csv");

    if (!res.ok) {
      console.error("[home.js] Fetch failed. Status:", res.status, "URL:", res.url);
      throw new Error(`CSV Not Found: ${res.status} ${res.statusText}`);
    }

    const contentType = res.headers.get("content-type") || "";
    const text = await res.text();

    console.log("[home.js] Fetch response received. Type:", contentType, "Length:", text.length);

    if (contentType.includes("text/html") || text.trim().startsWith("<!doctype")) {
      console.error("[home.js] Error: Server returned HTML instead of CSV. Check routing/404.");
      throw new Error("Dữ liệu nhận được là trang HTML (lỗi server/404), không phải file CSV.");
    }

    const parsed = parseCSV(text);
    console.log(`[home.js] Successfully parsed ${parsed.length} products.`);

    if (parsed.length === 0) {
      if (text.length > 0) {
        console.warn("[home.js] CSV text found but 0 products parsed. Check delimiter/header.");
        console.log("[home.js] Sample text:", text.substring(0, 100));
      } else {
        console.warn("[home.js] CSV file is empty.");
      }
    }

    return parsed;
  } catch (e) {
    console.error("[home.js] Failed to load products:", e);

    // Show error UI if we have no cached data
    const grid = $("allGrid");
    if (grid && state.products.length === 0) {
      grid.innerHTML = `<div style="grid-column: 1/-1; text-align: center; padding: 40px; color: #ef4444; background: #fff1f2; border: 1px solid #fecaca; border-radius: 12px;">
        <p style="font-size: 24px;">⚠️</p>
        <p style="font-weight: 700; margin-bottom: 8px;">KHÔNG THỂ TẢI DỮ LIỆU SẢN PHẨM</p>
        <p style="font-size: 0.9rem; color: #7f1d1d;">Mô tả: ${e.message}</p>
        <p style="font-size: 0.8rem; margin: 10px 0; color: #991b1b;">Hãy kiểm tra xem file <b>medicines_clean.csv</b> có nằm trong thư mục <b>web_app/data/</b> không.</p>
        <button onclick="window.location.reload()" class="btn btn--primary btn--sm" style="margin-top: 10px;">Thử tải lại (F5)</button>
      </div>`;
    }
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
      const hay = `${p.name} ${p.id} ${p.dosageForm}`.toLowerCase();
      return hay.includes(q);
    });
  }

  // Filter based on BASE UNIT PRICE
  if (min !== null) out = out.filter(p => p.finalBasePrice >= min);
  if (max !== null) out = out.filter(p => p.finalBasePrice <= max);

  if (state.onlySale) out = out.filter(p => p.discount > 0);

  out = sortProducts(out, state.sort);
  return out;
}

function sortProducts(arr, sortKey) {
  const a = [...arr];
  switch (sortKey) {
    case "price_asc":
      a.sort((x, y) => x.finalBasePrice - y.finalBasePrice);
      break;
    case "price_desc":
      a.sort((x, y) => y.finalBasePrice - x.finalBasePrice);
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

// -------- Pricing Logic --------
function calculateDisplayPrice(product, unit, qty = 1) {
  if (product.isLiquid) {
    // Liquid products: price per Chai (bottle)
    const original = product.price * qty;
    const final = product.finalBasePrice * qty;
    return { original, final };
  }

  // Solid products: Viên/Vỉ/Hộp
  let multiplier = 1;
  if (unit === "Vỉ") multiplier = product.vienPerVi;
  if (unit === "Hộp") multiplier = product.vienPerVi * product.viPerHop;

  const original = product.price * multiplier * qty;
  const final = product.finalBasePrice * multiplier * qty;

  return { original, final };
}

// -------- UI Rendering --------
function productCard(p) {
  const saleTag = p.discount > 0
    ? `<span class="tag tag--sale">SALE -${p.discount}%</span>`
    : `<span class="tag">NEW</span>`;

  // Display base price on card
  const unitLabel = p.isLiquid ? txt('unit_bottle') : txt('unit_pill');

  const { final } = calculateDisplayPrice(p, p.isLiquid ? "Chai" : "Viên");

  const priceHtml = p.discount > 0
    ? `<span class="price">${formatVND(final)}<span class="unit">/${unitLabel}</span> <del>${formatVND(p.price)}</del></span>`
    : `<span class="price">${formatVND(final)}<span class="unit">/${unitLabel}</span></span>`;

  return `
    <article class="card">
      <div class="card__top">
        <div>
          <h3 class="card__name" title="${escapeHtml(p.name)}">${escapeHtml(p.name)}</h3>
          <div class="card__meta">
            <div class="line">${txt('card.code')}: <b>${escapeHtml(p.id)}</b></div>
            <div class="line">${txt('card.type')}: <b>${escapeHtml(p.dosageForm)}</b> • ${txt('card.exp')}: <b>${escapeHtml(p.date)}</b></div>
          </div>
        </div>
        ${saleTag}
      </div>

      <div class="card__mid">
        ${priceHtml}
      </div>

      <div class="card__actions">
        <button class="btn btn--buy" onclick="openModal('${p.id}', true)">${txt('card.buy').toUpperCase()}</button>
        <button class="btn btn--add" onclick="openModal('${p.id}', false)">+ ${txt('nav_cart')}</button>
      </div>
    </article>
  `;
}

// -------- Modal Logic --------
let currentModalProductId = null;
let isBuyNow = false;

window.openModal = (id, buyNow) => {
  const p = state.products.find(x => x.id === id);
  if (!p) return;

  currentModalProductId = id;
  isBuyNow = buyNow;

  $("modalName").textContent = p.name;
  $("modalMeta").textContent = `${txt('lbl_code')}: ${p.id} • ${txt('lbl_type')}: ${p.dosageForm}`;

  // Populate Unit Selector based on product type
  const select = $("modalUnit");
  const vi = state.lang === "vi";
  if (p.isLiquid) {
    select.innerHTML = `<option value="Chai">${txt('unit_bottle')}</option>`;
    select.value = "Chai";
  } else {
    select.innerHTML = `
      <option value="Viên">${txt('unit_pill')}</option>
      <option value="Vỉ">${txt('unit_strip')} (x${p.vienPerVi})</option>
      <option value="Hộp">${txt('unit_box')} (x${p.vienPerVi * p.viPerHop})</option>
    `;
    select.value = "Viên";
  }

  // Reset quantity
  $("modalQty").value = 1;

  // Initial Price Update
  updateModalPrice();

  // Show Modal
  $("optModal").classList.add("active");

  // Setup listeners
  select.onchange = updateModalPrice;
  $("modalQty").oninput = updateModalPrice;
  $("modalConfirmBtn").onclick = handleModalConfirm;
};

window.closeModal = () => {
  $("optModal").classList.remove("active");
  currentModalProductId = null;
};

function updateModalPrice() {
  if (!currentModalProductId) return;
  const p = state.products.find(x => x.id === currentModalProductId);
  const unit = $("modalUnit").value;
  const qty = parseInt($("modalQty").value) || 1;
  const { original, final } = calculateDisplayPrice(p, unit, qty);

  const priceEl = $("modalPrice");
  if (p.discount > 0) {
    priceEl.innerHTML = `${formatVND(final)} <del style="font-size: 16px; color: #999; font-weight: 400;">${formatVND(original)}</del>`;
  } else {
    priceEl.textContent = formatVND(final);
  }
}

function handleModalConfirm() {
  if (!currentModalProductId) return;
  const unit = $("modalUnit").value;
  const qty = parseInt($("modalQty").value) || 1;
  addToCart(currentModalProductId, unit, qty);
  closeModal();

  if (isBuyNow) {
    // Redirect to checkout or similar if needed
    window.location.href = "cart.html";
  }
}

function addToCart(id, unit, qty) {
  const product = state.products.find((p) => p.id === id);
  if (!product) return;

  const { final } = calculateDisplayPrice(product, unit, 1); // price per unit

  let cart = getCart();
  const existing = cart.find(item => item.id === id && item.unit === unit);

  if (existing) {
    existing.qty += qty;
  } else {
    cart.push({
      id: product.id,
      name: product.name,
      price: final,
      unit: unit,
      qty: qty,
      isLiquid: product.isLiquid,
      vienPerVi: product.vienPerVi,
      viPerHop: product.viPerHop
    });
  }

  saveCart(cart);
  showToast(product.name);
}

function escapeHtml(str) {
  if (!str) return "";
  return String(str)
    .split("&").join("&amp;")
    .split("<").join("&lt;")
    .split(">").join("&gt;")
    .split('"').join("&quot;")
    .split("'").join("&#039;");
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

  if ($("resultsCount")) $("resultsCount").textContent = total.toLocaleString(state.lang === "vi" ? "vi-VN" : "en-US");

  const pagerDisplay = $("pagerDisplay");
  if (pagerDisplay) {
    let pageInfo = txt('page_info');
    pageInfo = pageInfo.replace('{now}', state.page).replace('{total}', totalPages);
    pagerDisplay.textContent = pageInfo;
  }

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
  const btnGoSale = $("btnGoSale");
  if (btnGoSale) {
    btnGoSale.onclick = () => {
      const section = $("saleSection");
      if (section) section.scrollIntoView({ behavior: "smooth" });
    };
  }

  // Set initial badge
  saveCart(getCart());

  // Global delegate for Buy/Add buttons - ONLY ONCE
  if (!isEventsBound) {
    document.body.addEventListener("click", (e) => {
      const buyId = e.target?.getAttribute?.("data-buy");
      const addId = e.target?.getAttribute?.("data-add");

      if (buyId || addId) {
        const id = buyId || addId;
        openModal(id, !!buyId);
      }
    });

    $("btnSearch").onclick = () => {
      state.query = $("globalSearch").value;
      state.page = 1;
      renderAllSections();
      scrollToAll();
    };

    $("globalSearch").onkeydown = (e) => {
      if (e.key === "Enter") $("btnSearch").click();
    };

    $("btnApply").onclick = () => {
      syncFiltersFromUI();
      state.page = 1;
      renderAllSections();
    };

    $("btnReset").onclick = () => {
      resetFiltersUI();
      syncFiltersFromUI();
      state.page = 1;
      renderAllSections();
    };

    $("prevPage").onclick = () => {
      state.page = Math.max(1, state.page - 1);
      renderAllSections();
      scrollToAll();
    };

    $("nextPage").onclick = () => {
      state.page += 1;
      renderAllSections();
      scrollToAll();
    };

    const debounced = debounce(() => {
      syncFiltersFromUI();
      state.page = 1;
      renderAllSections();
    }, 250);

    ["filterQuery", "filterMin", "filterMax"].forEach(id => {
      const el = $(id);
      if (el) el.oninput = debounced;
    });

    ["filterSort", "filterOnlySale"].forEach(id => {
      const el = $(id);
      if (el) el.onchange = () => {
        syncFiltersFromUI();
        state.page = 1;
        renderAllSections();
      };
    });

    $("btnGoAll").onclick = () => scrollToAll();
  }

  // Auth logic - Redirect to Profile Page
  const authState = window.authState || { isLoggedIn: false };
  const userMenuContainer = $("userMenuContainer");
  const btnLogin = $("btnLogin");

  if (authState.isLoggedIn) {
    userMenuContainer.innerHTML = `
      <div style="display: flex; align-items: center; gap: 8px;">
        <a href="profile.html" class="btn btn--ghost" style="text-decoration: none; display: flex; align-items: center; gap: 5px;">
          👤 ${escapeHtml(authState.fullName.split(' ').pop())}
        </a>
      </div>
    `;
  } else if (btnLogin) {
    btnLogin.onclick = () => {
      window.location.href = "login.html";
    };
  }

  const cartBtn = $("btnCart");
  if (cartBtn) {
    cartBtn.onclick = () => {
      window.location.href = "cart.html";
    };
  }

  // Language switcher
  const langBtn = $("btnLang");
  if (langBtn) {
    langBtn.onclick = switchLanguage;
  }
}

function syncFiltersFromUI() {
  state.query = $("filterQuery").value || "";
  state.min = $("filterMin").value || "";
  state.max = $("filterMax").value || "";
  state.sort = $("filterSort").value || "pop_desc";
  state.onlySale = $("filterOnlySale").checked;
  $("globalSearch").value = state.query;
}

function resetFiltersUI() {
  $("filterQuery").value = "";
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

// -------- Boot --------
let isEventsBound = false;

(async function init() {
  // 1. Initial UI Setup
  updateLocale();

  // 2. Instant Render from Cache (Stale-While-Revalidate)
  const cachedAuth = localStorage.getItem("cache_auth");
  const cachedProducts = localStorage.getItem("cache_products");

  if (cachedAuth) window.authState = JSON.parse(cachedAuth);
  if (cachedProducts) state.products = JSON.parse(cachedProducts);

  // Bind events exactly once
  if (!isEventsBound) {
    bindEvents();
    isEventsBound = true;
  }

  // Initial render if we have cache
  if (cachedProducts) renderAllSections();

  try {
    // 3. Fetch fresh data in background
    const [authRes, freshProducts] = await Promise.all([
      fetch("api/auth-status").then(r => r.ok ? r.json() : null).catch(() => null),
      loadProducts(state.products.length > 0) // Skip skeleton if we already have data
    ]);

    let needsReRender = false;

    // Update Auth
    if (authRes) {
      const authStr = JSON.stringify(authRes);
      if (authStr !== cachedAuth) {
        window.authState = authRes;
        localStorage.setItem("cache_auth", authStr);
        needsReRender = true;
      }
    } else if (cachedAuth) {
      window.authState = null;
      localStorage.removeItem("cache_auth");
      needsReRender = true;
    }

    // Update Products
    if (freshProducts && freshProducts.length > 0) {
      const productsStr = JSON.stringify(freshProducts);
      if (productsStr !== cachedProducts) {
        state.products = freshProducts;
        localStorage.setItem("cache_products", productsStr);
        needsReRender = true;
      }
    }

    // 4. Final render if data changed or first time
    if (needsReRender || !cachedProducts || state.products.length === 0) {
      if (state.products.length === 0 && (!freshProducts || freshProducts.length === 0)) {
        console.warn("[home.js] All load attempts failed. Using demo fallback.");
        state.products = [
          { id: "DEMO1", name: "Paracetamol 500mg", batchId: "B001", dosageForm: "Tablet", quantity: 100, price: 2000, finalBasePrice: 2000, discount: 0, popularity: 999, isLiquid: false },
          { id: "DEMO2", name: "Cebion Vitamin C", batchId: "B002", dosageForm: "Tablet", quantity: 50, price: 45000, finalBasePrice: 35000, discount: 20, popularity: 888, isLiquid: false }
        ];
      }
      renderAllSections();
      bindEvents();
    }
  } catch (e) {
    console.error("Init failed:", e);
    // Emergency render
    if (state.products.length === 0) {
      state.products = [{ id: "ERR1", name: "Lỗi tải dữ liệu", batchId: "ERROR", dosageForm: "System", quantity: 0, price: 0, finalBasePrice: 0, discount: 0, popularity: 0, isLiquid: false }];
    }
    renderAllSections();
  }
})();

// ═══════════════════════════════════════════════════════════════════
//  🌐 LANGUAGE SWITCHER SYSTEM
// ═══════════════════════════════════════════════════════════════════

const TRANSLATIONS = {
  vi: {
    // Meta
    app_title: "Github Pharmacy",
    // Header
    search_placeholder: "Tìm tên thuốc, mã thuốc...",
    login_button: "Đăng nhập",
    cart_button: "Giỏ hàng",
    // Hero
    hero_title: "Mua thuốc nhanh – tìm dễ – lọc chuẩn",
    hero_subtitle: "Cập nhật trực tiếp từ hệ thống quản lý kho.",
    hero_sale_button: "Xem Sale 🔥",
    hero_all_button: "Xem toàn bộ",
    pixel_card_foot: "Tư vấn tận tâm",
    // Sections
    sale_section_title: "Đang Sale",
    sale_section_subtitle: "Các sản phẩm giảm giá hấp dẫn",
    sale_empty: "Chưa có sản phẩm sale theo bộ lọc hiện tại.",
    bestseller_section_title: "Best Seller",
    bestseller_section_subtitle: "Sản phẩm được ưa chuộng nhất",
    bestseller_empty: "Chưa có best seller theo bộ lọc hiện tại.",
    all_products_section_title: "Tất cả sản phẩm",
    all_empty: "Không có sản phẩm phù hợp bộ lọc.",
    // Filters
    filters_title: "Bộ lọc",
    filter_keyword_label: "Từ khoá",
    filter_keyword_placeholder: "Tên thuốc, mã, lô...",
    filter_price_min_label: "Giá (min)",
    filter_price_max_label: "Giá (max)",
    filter_sort_label: "Sắp xếp",
    sort_pop_desc: "Phổ biến ↓",
    sort_price_asc: "Giá ↑",
    sort_price_desc: "Giá ↓",
    sort_date_desc: "Ngày nhập ↓",
    sort_name_asc: "Tên A→Z",
    filter_only_sale: "Chỉ hiện Sale",
    filter_reset_button: "Reset",
    filter_apply_button: "Áp dụng",
    // Pager
    pager_prev_button: "← Trước",
    pager_page_label: "Trang",
    pager_next_button: "Sau →",
    // Modal
    modal_unit_label: "Đơn vị tính",
    modal_quantity_label: "Số lượng",
    modal_cancel_button: "Huỷ",
    modal_add_to_cart_button: "Thêm vào giỏ",
    // Lang dropdown header
    lang_current_label: "🌐 Tiếng Việt",
  },
  en: {
    // Meta
    app_title: "Github Pharmacy",
    // Header
    search_placeholder: "Search medicine name, code...",
    login_button: "Login",
    cart_button: "Cart",
    // Hero
    hero_title: "Buy fast – find easy – filter smart",
    hero_subtitle: "Live updates from our inventory management system.",
    hero_sale_button: "View Sale 🔥",
    hero_all_button: "View all",
    pixel_card_foot: "Dedicated consultation",
    // Sections
    sale_section_title: "On Sale",
    sale_section_subtitle: "Attractive discounted products",
    sale_empty: "No sale products matching current filters.",
    bestseller_section_title: "Best Sellers",
    bestseller_section_subtitle: "Most popular products",
    bestseller_empty: "No best sellers matching current filters.",
    all_products_section_title: "All Products",
    all_empty: "No products match the current filters.",
    // Filters
    filters_title: "Filters",
    filter_keyword_label: "Keyword",
    filter_keyword_placeholder: "Medicine name, code, batch...",
    filter_price_min_label: "Price (min)",
    filter_price_max_label: "Price (max)",
    filter_sort_label: "Sort by",
    sort_pop_desc: "Popular ↓",
    sort_price_asc: "Price ↑",
    sort_price_desc: "Price ↓",
    sort_date_desc: "Import Date ↓",
    sort_name_asc: "Name A→Z",
    filter_only_sale: "Sale items only",
    filter_reset_button: "Reset",
    filter_apply_button: "Apply",
    // Pager
    pager_prev_button: "← Prev",
    pager_page_label: "Page",
    pager_next_button: "Next →",
    // Modal
    modal_unit_label: "Unit",
    modal_quantity_label: "Quantity",
    modal_cancel_button: "Cancel",
    modal_add_to_cart_button: "Add to cart",
    // Lang dropdown header
    lang_current_label: "🌐 English",
  },
};

function applyLanguage(lang) {
  const t = TRANSLATIONS[lang];
  if (!t) return;

  // Text content
  document.querySelectorAll("[data-i18n]").forEach((el) => {
    const key = el.getAttribute("data-i18n");
    if (t[key] !== undefined) el.textContent = t[key];
  });

  // Placeholders
  document.querySelectorAll("[data-i18n-placeholder]").forEach((el) => {
    const key = el.getAttribute("data-i18n-placeholder");
    if (t[key] !== undefined) el.placeholder = t[key];
  });

  // Page title
  if (t.app_title) document.title = t.app_title;

  // Update lang-current label in dropdown
  const langCurrentLabel = document.getElementById("langCurrentLabel");
  if (langCurrentLabel && t.lang_current_label) {
    langCurrentLabel.textContent = t.lang_current_label;
  }

  // Mark active option
  document.querySelectorAll(".lang-option").forEach((btn) => {
    btn.classList.toggle("active", btn.getAttribute("data-lang") === lang);
  });

  // Persist
  localStorage.setItem("app_lang", lang);
}

function showTayError() {
  // Remove any existing toast first
  const existing = document.getElementById("tayErrorToast");
  if (existing) existing.remove();

  const toast = document.createElement("div");
  toast.id = "tayErrorToast";
  toast.className = "tay-error-toast";
  toast.innerHTML = `
    <span>😂&nbsp; Bạn chưa đủ TÀY để chuyển đổi sang ngôn ngữ này</span>
    <button onclick="document.getElementById('tayErrorToast').remove()" aria-label="Đóng">✕</button>
  `;
  document.body.appendChild(toast);

  // Auto-dismiss after 4 seconds
  setTimeout(() => {
    if (toast.parentNode) toast.remove();
  }, 4000);
}

function initLangSwitcher() {
  const switcher = document.getElementById("langSwitcher");
  const btn = document.getElementById("langBtn");
  const dropdown = document.getElementById("langDropdown");
  if (!switcher || !btn || !dropdown) return;

  // Toggle dropdown
  btn.addEventListener("click", (e) => {
    e.stopPropagation();
    switcher.classList.toggle("open");
  });

  // Close when clicking outside
  document.addEventListener("click", (e) => {
    if (!switcher.contains(e.target)) {
      switcher.classList.remove("open");
    }
  });

  // Language option clicks
  dropdown.querySelectorAll(".lang-option").forEach((optBtn) => {
    optBtn.addEventListener("click", (e) => {
      const lang = optBtn.getAttribute("data-lang");

      if (lang === "tay") {
        showTayError();
      } else {
        applyLanguage(lang);
      }

      switcher.classList.remove("open");
    });
  });

  // Restore saved language on load
  const savedLang = localStorage.getItem("app_lang") || "vi";
  applyLanguage(savedLang);
}

// Boot language switcher after DOM ready
if (document.readyState === "loading") {
  document.addEventListener("DOMContentLoaded", initLangSwitcher);
} else {
  initLangSwitcher();
}
