const $ = (id) => document.getElementById(id);

function formatVND(n) {
  return n.toLocaleString("vi-VN") + "đ";
}

function getCart() {
  return JSON.parse(localStorage.getItem("cart") || "[]");
}

function saveCart(cart) {
  localStorage.setItem("cart", JSON.stringify(cart));
  renderCart();
}

function getUnitConversionInfo(item) {
  if (item.isLiquid) return '';
  if (item.unit !== 'Viên') return '';

  const vienPerVi = item.vienPerVi || 10;
  const viPerHop = item.viPerHop || 3;
  const vienPerHop = vienPerVi * viPerHop;

  if (item.qty >= vienPerHop) {
    const hop = Math.floor(item.qty / vienPerHop);
    const remainder = item.qty % vienPerHop;
    if (remainder === 0) {
      return `<span class="unit-convert">= ${hop} ${window.t ? window.t('common', 'unit_box') : 'Hộp'}</span>`;
    } else {
      return `<span class="unit-convert">= ${hop} ${window.t ? window.t('common', 'unit_box') : 'Hộp'} + ${remainder} ${window.t ? window.t('common', 'unit_pill') : 'Viên'}</span>`;
    }
  } else if (item.qty >= vienPerVi) {
    const vi = Math.floor(item.qty / vienPerVi);
    const remainder = item.qty % vienPerVi;
    if (remainder === 0) {
      return `<span class="unit-convert">= ${vi} ${window.t ? window.t('common', 'unit_strip') : 'Vỉ'}</span>`;
    } else {
      return `<span class="unit-convert">= ${vi} ${window.t ? window.t('common', 'unit_strip') : 'Vỉ'} + ${remainder} ${window.t ? window.t('common', 'unit_pill') : 'Viên'}</span>`;
    }
  }
  return '';
}

function renderCart() {
  const cart = getCart();
  const list = $("cartList");

  if (cart.length === 0) {
    list.innerHTML = `
      <div style="text-align: center; padding: 40px;">
        <p class="muted" data-i18n="cart.empty">${window.t ? window.t('cart', 'empty') : 'Your cart is empty'}</p>
        <a href="home.html" class="btn btn--primary" style="margin-top: 20px;" data-i18n="cart.btn_continue">${window.t ? window.t('cart', 'btn_continue') : 'Continue Shopping'}</a>
      </div>
    `;
    $("subtotal").textContent = "0đ";
    $("totalPrice").textContent = "0đ";
    return;
  }

  let subtotal = 0;
  list.innerHTML = cart.map((item, index) => {
    const itemTotal = item.price * item.qty;
    subtotal += itemTotal;
    const conversionInfo = getUnitConversionInfo(item);

    // Internationalize dynamic labels
    const lblCode = window.t ? window.t('common', 'lbl_code') : 'Code';
    const lblType = window.t ? window.t('common', 'lbl_type') : 'Type';

    // Map unit internal values to translated names
    const unitMap = {
      'Viên': window.t ? window.t('common', 'unit_pill') : 'pill',
      'Vỉ': window.t ? window.t('common', 'unit_strip') : 'strip',
      'Hộp': window.t ? window.t('common', 'unit_box') : 'box',
      'Chai': window.t ? window.t('common', 'unit_bottle') : 'bottle'
    };
    const unitDisplay = unitMap[item.unit] || item.unit;

    return `
      <div class="cart-item">
        <div class="cart-item__info">
          <h3>${item.name}</h3>
          <p>${lblCode}: ${item.id} • ${lblType}: <b>${unitDisplay}</b></p>
        </div>
        <div class="cart-item__qty">
          <button class="qty-btn" onclick="updateQty(${index}, -1)">-</button>
          <input type="number" class="qty-input" value="${item.qty}" min="1" onchange="setQty(${index}, this.value)" />
          <button class="qty-btn" onclick="updateQty(${index}, 1)">+</button>
          ${conversionInfo}
        </div>
        <div style="font-weight: 700; color: var(--primary); min-width: 100px; text-align: right;">
          ${formatVND(itemTotal)}
        </div>
        <button class="btn" style="color: var(--danger); padding: 8px;" onclick="removeItem(${index})">🗑</button>
      </div>
    `;
  }).join("");

  $("subtotal").textContent = formatVND(subtotal);
  $("totalPrice").textContent = formatVND(subtotal);
}

// Helper for auto-conversion (Viên -> Hộp/Vỉ)
function checkConvert(item) {
  if (item.isLiquid) return;

  // Use defaults if missing (same as parseCSV/getUnitConversionInfo)
  const perVi = item.vienPerVi || 10;
  const viPerHop = item.viPerHop || 3;
  const perHop = perVi * viPerHop;

  if (item.unit !== 'Viên') return;

  if (item.qty >= perHop && item.qty % perHop === 0) {
    // Convert to Box
    const ratio = item.qty / perHop;
    item.unit = 'Hộp';
    item.qty = ratio;
    // Price remains the unit price for the NEW unit? 
    // Wait, cart item price is usually stored as UNITY Price. 
    // If we change unit, we must update price to be the Price Per Box.
    // In renderCart, itemTotal = item.price * item.qty.
    // If we had 30 pills * 1k = 30k.
    // Now 1 Box * 30k = 30k.
    // So item.price must be multiplied by perHop.
    item.price = item.price * perHop;
  } else if (item.qty >= perVi && item.qty % perVi === 0) {
    // Convert to Blister (Vỉ)
    const ratio = item.qty / perVi;
    item.unit = 'Vỉ';
    item.qty = ratio;
    item.price = item.price * perVi;
  }
}

window.updateQty = (index, delta) => {
  const cart = getCart();
  const item = cart[index];

  // If user increases quantity, we just increase current unit quantity
  // But if the RESULT triggers a conversion threshold (e.g. user had 29 Viên, adds 1 -> 30 Viên), we convert.
  item.qty = Math.max(1, item.qty + delta);

  // Try conversion
  checkConvert(item);

  saveCart(cart);
};

window.setQty = (index, value) => {
  const cart = getCart();
  const qty = parseInt(value) || 1;
  const item = cart[index];

  item.qty = Math.max(1, qty);

  // Try conversion
  checkConvert(item);

  saveCart(cart);
};

window.removeItem = (index) => {
  const cart = getCart();
  cart.splice(index, 1);
  saveCart(cart);
};

// Checkout button
document.addEventListener('DOMContentLoaded', () => {
  const btnCheckout = $("btnCheckout");
  if (btnCheckout) {
    btnCheckout.addEventListener('click', () => {
      const cart = getCart();
      if (cart.length === 0) {
        alert(window.t ? window.t('cart', 'empty') : 'Giỏ hàng trống!');
        return;
      }
      window.location.href = 'checkout.html';
    });
  }
});

// Boot
renderCart();
