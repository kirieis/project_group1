const $ = (id) => document.getElementById(id);

function formatVND(n) {
    return n.toLocaleString("vi-VN") + "đ";
}

function getCart() {
    return JSON.parse(localStorage.getItem("cart") || "[]");
}

function clearCart() {
    localStorage.removeItem("cart");
}

// ==== DISCOUNT CODES ====
const DISCOUNT_CODES = {
    'A7CO': 7,
    'ANHSITA': 10,
    'THANHHOA': 36
};

let appliedDiscount = 0;
let subtotal = 0;
let isLoggedIn = false;
let currentUser = null;
let pollingInterval = null;
let currentInvoiceId = null;

// ==== CHECK LOGIN ====
async function checkLoginStatus() {
    try {
        const res = await fetch('api/auth-status');
        const data = await res.json();
        isLoggedIn = data.isLoggedIn;
        currentUser = data.isLoggedIn ? data : null;
        updateDiscountUI();
    } catch (e) {
        isLoggedIn = false;
        currentUser = null;
        updateDiscountUI();
    }
}

function updateDiscountUI() {
    const discountRow = document.querySelector('.discount-row');
    if (!discountRow) return;
    if (!isLoggedIn) {
        discountRow.innerHTML = `
            <div style="width: 100%; text-align: center; padding: 12px; background: #fef3c7; border-radius: 8px; border: 1px solid #fcd34d;">
                <span style="color: #92400e;">🔒 <a href="login.html" style="color: #1d4ed8; text-decoration: underline; font-weight: 600;">${txt('login_to_discount')}</a></span>
            </div>
        `;
    }
}

// ==== RENDER ITEMS ====
function renderOrderItems() {
    const cart = getCart();
    const container = $("orderItems");
    if (!container) return;

    if (cart.length === 0) {
        container.innerHTML = `<p class="muted">${window.t ? window.t('cart', 'empty') : 'Your cart is empty'}</p>`;
        return;
    }

    subtotal = 0;
    container.innerHTML = cart.map(item => {
        const itemTotal = item.price * item.qty;
        subtotal += itemTotal;
        const unitDisplay = item.unit === 'Viên' ? (window.t ? window.t('common', 'unit_pill') : 'pill') :
            item.unit === 'Vỉ' ? (window.t ? window.t('common', 'unit_strip') : 'strip') :
                item.unit === 'Hộp' ? (window.t ? window.t('common', 'unit_box') : 'box') :
                    item.unit === 'Chai' ? (window.t ? window.t('common', 'unit_bottle') : 'bottle') : item.unit;
        return `
            <div class="order-item">
                <div>
                    <div style="font-weight: 600;">${item.name}</div>
                    <div style="font-size: 13px; color: var(--text-muted);">${item.qty} ${unitDisplay} × ${formatVND(item.price)}</div>
                </div>
                <div style="font-weight: 700; color: var(--primary);">
                    ${formatVND(itemTotal)}
                </div>
            </div>
        `;
    }).join("");

    updatePrices();
}

// ==== UPDATE PRICES ====
function updatePrices() {
    const subtotalEl = $("subtotal");
    const totalPriceEl = $("totalPrice");
    const discountLineEl = $("discountLine");
    const discountAmountEl = $("discountAmount");

    if (subtotalEl) subtotalEl.textContent = formatVND(subtotal);

    const discountAmount = Math.round(subtotal * appliedDiscount / 100);
    const total = subtotal - discountAmount;

    if (appliedDiscount > 0 && discountLineEl) {
        discountLineEl.style.display = "flex";
        if (discountAmountEl) discountAmountEl.textContent = `-${formatVND(discountAmount)} (${appliedDiscount}%)`;
    } else if (discountLineEl) {
        discountLineEl.style.display = "none";
    }

    if (totalPriceEl) totalPriceEl.textContent = formatVND(total);
}

function getFinalAmount() {
    return subtotal - Math.round(subtotal * appliedDiscount / 100);
}

// ==== APPLY DISCOUNT ====
function applyDiscount() {
    const messageEl = $("discountMessage");
    if (!messageEl) return;

    if (!isLoggedIn) {
        messageEl.innerHTML = `<span style="color: var(--danger);">⛔ ${txt('login_to_discount')}!</span>`;
        return;
    }

    const codeEl = $("discountCode");
    if (!codeEl) return;
    const code = codeEl.value.trim().toUpperCase();

    if (!code) {
        messageEl.innerHTML = `<span style="color: var(--danger);">${txt('empty_discount')}</span>`;
        return;
    }

    if (code === 'ADMIN_FREE') {
        const userRole = (currentUser && currentUser.role) ? currentUser.role.toUpperCase() : "";
        if (isLoggedIn && userRole === 'ADMIN') {
            appliedDiscount = 100;
            messageEl.innerHTML = `<span style="color: #6366f1; font-weight: 700;">🛡️ ADMIN: FREE SALE ON!</span>`;
            updatePrices();
            return;
        } else {
            messageEl.innerHTML = `<span style="color: var(--danger);">⛔ Admin Only!</span>`;
            return;
        }
    }

    if (DISCOUNT_CODES[code]) {
        appliedDiscount = DISCOUNT_CODES[code];
        messageEl.innerHTML = `<span style="color: green; font-weight: 600;">✓ ${txt('applied_discount')} "${code}" (-${appliedDiscount}%)</span>`;
        updatePrices();
    } else {
        messageEl.innerHTML = `<span style="color: var(--danger);">✗ ${txt('invalid_discount')}</span>`;
        appliedDiscount = 0;
        updatePrices();
    }
}

// ==== CONFIRM PAYMENT ====
async function confirmPayment() {
    const cart = getCart();
    if (cart.length === 0) {
        alert(window.t ? window.t('cart', 'empty') : 'Cart is empty!');
        return;
    }

    const btnConfirm = $("btnConfirmPayment");
    btnConfirm.disabled = true;
    btnConfirm.classList.add("btn-disabled");
    btnConfirm.innerHTML = `⏳ ${txt('initializing_order')}`;

    const finalAmount = getFinalAmount();

    try {
        const res = await fetch('api/sepay/create-order', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ totalAmount: finalAmount, items: cart })
        });

        const result = await res.json();

        if (res.ok && result.invoiceId) {
            currentInvoiceId = result.invoiceId;
            clearCart();
            showQRModal(currentInvoiceId, finalAmount);
            startPollingOrderStatus(currentInvoiceId);
        } else {
            alert('Error: ' + (result.message || 'Cannot create order'));
            resetConfirmButton();
        }
    } catch (e) {
        console.error("Payment error:", e);
        alert('Connection Error!');
        resetConfirmButton();
    }
}

function resetConfirmButton() {
    const btn = $("btnConfirmPayment");
    if (!btn) return;
    btn.disabled = false;
    btn.classList.remove("btn-disabled");
    btn.innerHTML = `🚀 ${txt('btn_confirm')}`;
}

// ════════════════════════════════════════════════════════════════════
// MODAL QR CODE - PREMIUM DESIGN
// ════════════════════════════════════════════════════════════════════
// Helper for checkout page specifically
const txt = (key) => window.t ? window.t('checkout', key) : key;

function showQRModal(invoiceId, amount) {
    const modal = $("successModal");
    const content = modal.querySelector(".success-content");

    const qrUrl = `https://qr.sepay.vn/img?bank=MB&acc=3399377355&template=compact&amount=${amount}&des=DH${invoiceId}`;

    content.innerHTML = `
        <div style="padding: 10px;">
            <!-- Header -->
            <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px;">
                <div style="display: flex; align-items: center; gap: 10px;">
                    <div style="width: 40px; height: 40px; background: linear-gradient(135deg, #6366f1, #8b5cf6); border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 20px;">💳</div>
                    <div>
                        <div style="font-weight: 700; color: #1e293b; font-size: 16px;">${txt('qr_title')}</div>
                        <div style="font-size: 12px; color: #94a3b8;">${txt('order_id')} #${invoiceId}</div>
                    </div>
                </div>
                <div style="background: #fef3c7; color: #92400e; padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: 600;">
                    ⏳ ${window.t ? window.t('profile', 'order_status_pending').replace('⏳ ', '') : 'Pending'}
                </div>
            </div>

            <!-- QR Code -->
            <div style="background: linear-gradient(135deg, #f0f4ff 0%, #e8ecf9 100%); border-radius: 16px; padding: 20px; margin-bottom: 16px;">
                <div style="background: white; border-radius: 12px; padding: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.08); display: inline-block;">
                    <img src="${qrUrl}" alt="VietQR" style="width: 200px; height: auto; display: block; border-radius: 8px;">
                </div>
                <p style="margin-top: 12px; font-size: 12px; color: #64748b;">${txt('qr_desc')}</p>
            </div>

            <!-- Info -->
            <div style="background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px; padding: 14px; margin-bottom: 16px;">
                <div style="display: flex; justify-content: space-between; margin-bottom: 8px;">
                    <span style="color: #94a3b8; font-size: 13px;">${txt('amount')}</span>
                    <span style="color: #dc2626; font-weight: 800; font-size: 16px;">${formatVND(amount)}</span>
                </div>
                <div style="display: flex; justify-content: space-between; margin-bottom: 8px;">
                    <span style="color: #94a3b8; font-size: 13px;">${txt('transfer_content')}</span>
                    <span style="color: #1e40af; font-weight: 800;">DH${invoiceId}</span>
                </div>
                <div style="display: flex; justify-content: space-between;">
                    <span style="color: #94a3b8; font-size: 13px;">${txt('bank')}</span>
                    <span style="color: #1e293b; font-weight: 600;">MB Bank</span>
                </div>
            </div>

            <!-- Loading indicator -->
            <div id="qrStatusIndicator" style="display: flex; align-items: center; justify-content: center; gap: 8px; margin-bottom: 16px;">
                <div style="width: 16px; height: 16px; border: 2px solid #e2e8f0; border-top-color: #6366f1; border-radius: 50%; animation: spin 0.8s linear infinite;"></div>
                <span style="font-size: 13px; color: #64748b;">${txt('waiting_payment')}</span>
            </div>

            <!-- Cancel Button -->
            <button onclick="cancelOrder()" 
                style="width: 100%; padding: 14px; background: none; border: 2px solid #fed7d7; color: #e53e3e; border-radius: 12px; font-size: 14px; font-weight: 700; cursor: pointer; transition: all 0.2s; font-family: inherit;"
                onmouseover="this.style.background='#fff5f5'; this.style.borderColor='#fc8181'"
                onmouseout="this.style.background='none'; this.style.borderColor='#fed7d7'">
                ✕ ${txt('cancel_btn')}
            </button>

            <p style="margin-top: 10px; font-size: 11px; color: #cbd5e1; line-height: 1.4;">
                ${txt('security_note')}
            </p>
        </div>
    `;

    modal.classList.add("active");
}

// ==== CANCEL ORDER ====
async function cancelOrder() {
    if (!currentInvoiceId) {
        closeModal();
        return;
    }

    const confirmed = confirm(txt('cancel_order_confirm'));
    if (!confirmed) return;

    // Dừng polling
    if (pollingInterval) clearInterval(pollingInterval);

    try {
        // Gọi API cập nhật trạng thái thành CANCELLED
        const res = await fetch('api/orders', {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ orderId: currentInvoiceId, status: 'CANCELLED' })
        });

        // Hiện thông báo hủy thành công
        const modal = $("successModal");
        const content = modal.querySelector(".success-content");
        content.innerHTML = `
            <div style="text-align: center; padding: 20px;">
                <div style="font-size: 56px; margin-bottom: 12px;">🗑️</div>
                <h2 style="color: #e53e3e; margin-bottom: 8px; font-size: 20px;">${txt('order_cancelled')}</h2>
                <p style="color: #64748b; margin-bottom: 20px; font-size: 14px;">
                    ${txt('order_id')} #${currentInvoiceId} ${txt('order_cancelled_desc')}
                </p>
                <button class="btn btn--primary" onclick="window.location.href='home.html'" 
                    style="width: 100%; justify-content: center; padding: 14px;">
                    ← ${window.t ? window.t('common', 'btn_back_home') : 'Back to Home'}
                </button>
            </div>
        `;
    } catch (e) {
        console.error("Cancel error:", e);
        alert("Error!");
    }
}

function closeModal() {
    const modal = $("successModal");
    if (modal) modal.classList.remove("active");
    if (pollingInterval) clearInterval(pollingInterval);
}

// ==== POLLING ORDER STATUS ====
function startPollingOrderStatus(invoiceId) {
    let tried = 0;
    pollingInterval = setInterval(async () => {
        tried++;
        try {
            const res = await fetch(`api/orders/status?invoiceId=${invoiceId}`);
            if (res.ok) {
                const data = await res.json();
                if (data.status === 'PAID') {
                    clearInterval(pollingInterval);
                    showPaymentSuccess(invoiceId);
                    return;
                }
            }
        } catch (e) { }

        if (tried >= 60) {
            clearInterval(pollingInterval);
            showPaymentTimeout(invoiceId);
        }
    }, 5000);
}

function showPaymentSuccess(invoiceId) {
    const modal = $("successModal");
    const content = modal.querySelector(".success-content");
    content.innerHTML = `
        <div style="text-align: center; padding: 20px;">
            <div style="font-size: 64px; margin-bottom: 12px;">✅</div>
            <h2 style="color: #166534; margin-bottom: 8px;">${txt('payment_success')}</h2>
            <p style="color: #64748b; margin-bottom: 20px;">${txt('order_id')} #${invoiceId} ${txt('payment_success_desc')}</p>
            <div style="display: flex; gap: 10px;">
                <button class="btn btn--ghost" onclick="window.location.href='home.html'" style="flex: 1; justify-content: center; background: var(--light-bg); color: var(--primary); border: 1px solid var(--border);">${window.t ? window.t('common', 'nav_home') : 'Home'}</button>
                <button class="btn btn--primary" onclick="window.location.href='profile.html'" style="flex: 1; justify-content: center;">${window.t ? window.t('profile', 'tab_history') : 'View Orders'}</button>
            </div>
        </div>
    `;
}

function showPaymentTimeout(invoiceId) {
    const modal = $("successModal");
    const content = modal.querySelector(".success-content");
    content.innerHTML = `
        <div style="text-align: center; padding: 20px;">
            <div style="font-size: 56px; margin-bottom: 12px;">⏰</div>
            <h2 style="color: #b45309; margin-bottom: 8px;">${txt('not_received_yet')}</h2>
            <p style="color: #64748b; margin-bottom: 20px; font-size: 14px;">
                ${txt('order_id')} #${invoiceId} ${txt('not_received_yet_desc')}
            </p>
            <div style="display: flex; gap: 10px;">
                <button class="btn btn--ghost" onclick="cancelOrder()" style="flex: 1; justify-content: center; background: var(--light-bg); color: #e53e3e; border: 1px solid #fed7d7;">${txt('cancel_btn')}</button>
                <button class="btn btn--primary" onclick="window.location.href='profile.html'" style="flex: 1; justify-content: center;">${window.t ? window.t('profile', 'tab_history') : 'View Orders'}</button>
            </div>
        </div>
    `;
}

document.addEventListener('DOMContentLoaded', async () => {
    await checkLoginStatus();
    renderOrderItems();
    if ($("btnApplyDiscount")) $("btnApplyDiscount").addEventListener('click', applyDiscount);
    if ($("btnConfirmPayment")) $("btnConfirmPayment").addEventListener('click', confirmPayment);
});
