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
  pageSize: 9,
};

// Removed local i18n object - now using global i18n.js

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
  t.innerHTML = `🛒 <span>${msg}</span> ${'đã được thêm vào giỏ!'}`;
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
    : `<span class="tag">MỚI</span>`;

  // Display base price on card
  const unitLabel = p.isLiquid ? 'chai' : 'viên';

  const { final } = calculateDisplayPrice(p, p.isLiquid ? "Chai" : "Viên");

  const priceHtml = p.discount > 0
    ? `<span class="price">${formatVND(final)}<span class="unit">/${unitLabel}</span> <del>${formatVND(p.price)}</del></span>`
    : `<span class="price">${formatVND(final)}<span class="unit">/${unitLabel}</span></span>`;

  // Format expiry date nicely
  let expiryDisplay = p.date || 'N/A';
  let expiryClass = 'info-pill info-pill--expiry';
  if (p.date) {
    let d = new Date(p.date);
    if (!isNaN(d.getTime())) {
      const diff = d - new Date();
      const daysLeft = Math.ceil(diff / (1000 * 60 * 60 * 24));
      if (daysLeft <= 90 && daysLeft > 0) {
        expiryClass += ' expiring-soon';
        expiryDisplay = p.date + ' ⚠️';
      } else if (daysLeft <= 0) {
        expiryClass += ' expiring-soon';
        expiryDisplay = 'Đã hết hạn ❌';
      }
    }
  }

  const productType = p.isLiquid ? 'liquid' : 'solid';
  const unitIcon = p.isLiquid ? '🧴' : '💊';

  return `
    <article class="card" data-type="${productType}">
      <div class="card__top">
        <div>
          <h3 class="card__name" title="${escapeHtml(p.name)}">${escapeHtml(p.name)}</h3>
          <div class="card__meta">
            <div class="line">Mã: <b>${escapeHtml(p.id)}</b></div>
          </div>
        </div>
        ${saleTag}
      </div>

      <div class="card__info">
        <div class="card__info-line">
          <span class="info-pill">📦 <b>${escapeHtml(p.dosageForm)}</b></span>
          <span class="info-pill info-pill--unit info-pill--unit-${productType}">${unitIcon} <b>${unitLabel}</b></span>
        </div>
        <div class="card__info-line">
          <span class="${expiryClass}">📅 HSD: <b>${escapeHtml(expiryDisplay)}</b></span>
        </div>
      </div>

      <div class="card__mid">
        ${priceHtml}
      </div>

      <div class="card__actions">
        <button class="btn btn--buy" onclick="openModal('${p.id}', true)">MUA</button>
        <button class="btn btn--add" onclick="openModal('${p.id}', false)">+ GIỎ HÀNG</button>
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
  $("modalMeta").textContent = `${'Mã'}: ${p.id} • ${'Loại'}: ${p.dosageForm}`;

  // Populate Unit Selector based on product type
  const select = $("modalUnit");
  const vi = state.lang === "vi";
  if (p.isLiquid) {
    select.innerHTML = `<option value="Chai">${'chai'}</option>`;
    select.value = "Chai";
  } else {
    select.innerHTML = `
      <option value="Viên">${'viên'}</option>
      <option value="Vỉ">${'vỉ'} (x${p.vienPerVi})</option>
      <option value="Hộp">${'hộp'} (x${p.vienPerVi * p.viPerHop})</option>
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
  const saleGrid = $("saleGrid");

  if (uniqueSale.length > 0) {
    // Duplicate items for seamless infinite scroll
    const cards = uniqueSale.map(productCard).join("");
    saleGrid.innerHTML = `
      <div class="carousel-wrapper">
        <div id="saleTrack" class="carousel-track">
          ${cards}${cards}
        </div>
      </div>`;
    saleGrid.className = '';  // Remove grid class
    startCarouselAutoScroll("saleTrack");
  } else {
    saleGrid.innerHTML = '';
    saleGrid.className = 'grid';
  }
  $("saleEmpty").classList.toggle("hidden", uniqueSale.length > 0);
}

function renderBest(filtered) {
  const uniqueBest = [...filtered].sort((a, b) => b.popularity - a.popularity).slice(0, 8);
  const bestGrid = $("bestGrid");

  if (uniqueBest.length > 0) {
    const cards = uniqueBest.map(productCard).join("");
    bestGrid.innerHTML = `
      <div class="carousel-wrapper">
        <div id="bestTrack" class="carousel-track" style="animation-direction: reverse;">
          ${cards}${cards}
        </div>
      </div>`;
    bestGrid.className = '';  // Remove grid class
    startCarouselAutoScroll("bestTrack");
  } else {
    bestGrid.innerHTML = '';
    bestGrid.className = 'grid';
  }
  $("bestEmpty").classList.toggle("hidden", uniqueBest.length > 0);
}

function renderAll(filtered) {
  const { items, total, totalPages } = paginate(filtered);

  if ($("resultsCount")) $("resultsCount").textContent = total.toLocaleString(state.lang === "vi" ? "vi-VN" : "en-US");

  const pagerDisplay = $("pagerDisplay");
  if (pagerDisplay) {
    let pageInfo = 'Trang {now} / {total}';
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

  // featured sections always show global top items
  renderSale(uniqueProducts);
  renderBest(uniqueProducts);

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

  // Auth logic - Redirect to Profile Page + Admin Portal
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

// -------- Smooth Carousel Auto-Scroll (Manual-Friendly) --------
const activeCarousels = new Map();

function startCarouselAutoScroll(trackId) {
  const track = document.getElementById(trackId);
  if (!track || track.children.length < 2) return;

  // Clear previous animation if it exists
  if (activeCarousels.has(trackId)) {
    cancelAnimationFrame(activeCarousels.get(trackId));
  }

  let isPaused = false;
  let isDragging = false;
  const isReverse = track.style.animationDirection === 'reverse';
  const speed = isReverse ? -0.5 : 0.5;

  // Find exact loop point (offset of the first card in the second set)
  const half = track.children[Math.floor(track.children.length / 2)].offsetLeft;

  // For reverse, start in the middle
  if (isReverse) track.scrollLeft = half;

  const loop = () => {
    if (!isPaused && !isDragging) {
      track.scrollLeft += speed;
      // Seamless wrap
      if (track.scrollLeft >= half) track.scrollLeft -= half;
      if (track.scrollLeft < 0) track.scrollLeft += half;
    }
    activeCarousels.set(trackId, requestAnimationFrame(loop));
  };

  // Interaction events
  track.onmouseenter = () => { isPaused = true; };
  track.onmouseleave = () => {
    isPaused = false;
    isDragging = false;
    track.classList.remove('active');
  };

  let startX;
  let scrollLeftStart;
  let hasMoved = false;

  track.onmousedown = (e) => {
    isDragging = true;
    hasMoved = false;
    track.classList.add('active');
    startX = e.pageX;
    scrollLeftStart = track.scrollLeft;
    // Do NOT disable pointer-events here - only after actual drag movement
  };

  track.onmousemove = (e) => {
    if (!isDragging) return;
    const x = e.pageX;
    const walk = (x - startX) * 1.5;

    // Only disable clicks after significant mouse movement (real drag, not accidental)
    if (!hasMoved && Math.abs(e.pageX - startX) > 5) {
      hasMoved = true;
      Array.from(track.children).forEach(c => c.style.pointerEvents = 'none');
    }

    if (!hasMoved) return;

    let newScroll = scrollLeftStart - walk;

    // Infinite wrap while dragging
    if (newScroll >= half) {
      newScroll -= half;
      startX = x;
      scrollLeftStart = newScroll;
    } else if (newScroll < 0) {
      newScroll += half;
      startX = x;
      scrollLeftStart = newScroll;
    }

    track.scrollLeft = newScroll;
  };

  const stopDrag = () => {
    if (!isDragging) return;
    isDragging = false;
    track.classList.remove('active');
    // Re-enable clicks after a tiny delay so it doesn't fire click on mouseup
    if (hasMoved) {
      setTimeout(() => {
        Array.from(track.children).forEach(c => c.style.pointerEvents = '');
      }, 50);
    }
    hasMoved = false;
  };

  window.addEventListener('mouseup', stopDrag, { passive: true });

  // Touch relies on native scroll, just pause the auto loop
  track.ontouchstart = () => { isPaused = true; };
  track.ontouchend = () => { isPaused = false; };

  activeCarousels.set(trackId, requestAnimationFrame(loop));
}

// -------- Boot --------
let isEventsBound = false;

(async function init() {
  // 1. Initial UI Setup
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

// Boot features after DOM ready
if (document.readyState === "loading") {
  document.addEventListener("DOMContentLoaded", () => {
    initLangSwitcher();
    initCarouselAutoScroll();
  });
} else {
  initLangSwitcher();
  initCarouselAutoScroll();
}

function initCarouselAutoScroll() {
  const tracks = document.querySelectorAll('.carousel-track');

  tracks.forEach(track => {
    let isDown = false;
    let startX;
    let scrollLeft;

    // --- MOUSE DRAG LOGIC ---
    track.addEventListener('mousedown', (e) => {
      isDown = true;
      track.classList.add('active'); // Optional: for styling cursor
      startX = e.pageX - track.offsetLeft;
      scrollLeft = track.scrollLeft;
    });

    track.addEventListener('mouseleave', () => {
      isDown = false;
      track.classList.remove('active');
    });

    track.addEventListener('mouseup', () => {
      isDown = false;
      track.classList.remove('active');
    });

    track.addEventListener('mousemove', (e) => {
      if (!isDown) return;
      e.preventDefault();
      const x = e.pageX - track.offsetLeft;
      const walk = (x - startX) * 2; // Tốc độ kéo
      track.scrollLeft = scrollLeft - walk;
    });
  });

  // --- AUTO SCROLL TIMER ---
  setInterval(() => {
    tracks.forEach(track => {
      // Chỉ chạy nếu KHÔNG bị hover VÀ KHÔNG đang bị nhấn giữ kéo
      const isBeingDragged = track.classList.contains('active');
      if (!track.matches(':hover')) {
        track.scrollLeft += 1;
        if (track.scrollLeft >= (track.scrollWidth - track.clientWidth - 1)) {
          track.scrollLeft = 0;
        }
      }
    });
  }, 30);
}
