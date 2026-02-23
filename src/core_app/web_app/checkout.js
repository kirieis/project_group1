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

// Discount codes
const DISCOUNT_CODES = {
    'A7CO': 7,
    'ANHSITA': 10,
    'THANHHOA': 36
};

let appliedDiscount = 0;
let subtotal = 0;
let isLoggedIn = false;
let currentUser = null;

function generateOrderId() {
    const timestamp = Date.now().toString(36).toUpperCase();
    const random = Math.random().toString(36).substring(2, 5).toUpperCase();
    return `ĐH-${timestamp}${random}`;
}

const orderId = generateOrderId();

// Check login status from backend
async function checkLoginStatus() {
    try {
        const res = await fetch('api/auth-status');
        const data = await res.json();
        console.log("DEBUG: Login Status Received:", data);
        isLoggedIn = data.isLoggedIn;
        currentUser = data.isLoggedIn ? data : null;

        // Update UI based on login status
        updateDiscountUI();
    } catch (e) {
        // If fetch fails (e.g., running on Live Server), default to not logged in
        isLoggedIn = false;
        currentUser = null;
        updateDiscountUI();
    }
}

function updateDiscountUI() {
    const discountRow = document.querySelector('.discount-row');
    if (!discountRow) return;

    if (!isLoggedIn) {
        // Show login prompt instead of discount input
        discountRow.innerHTML = `
            <div style="width: 100%; text-align: center; padding: 12px; background: #fef3c7; border-radius: 8px; border: 1px solid #fcd34d;">
                <span style="color: #92400e;">🔒 <a href="login.html" style="color: #1d4ed8; text-decoration: underline; font-weight: 600;">Đăng nhập</a> để sử dụng mã giảm giá</span>
            </div>
        `;
    }
}

function renderOrderItems() {
    const cart = getCart();
    const container = $("orderItems");

    if (cart.length === 0) {
        container.innerHTML = '<p class="muted">Không có sản phẩm nào trong đơn hàng</p>';
        return;
    }

    subtotal = 0;
    container.innerHTML = cart.map(item => {
        const itemTotal = item.price * item.qty;
        subtotal += itemTotal;
        return `
            <div class="order-item">
                <div>
                    <div style="font-weight: 600;">${item.name}</div>
                    <div style="font-size: 13px; color: var(--text-muted);">${item.qty} ${item.unit} × ${formatVND(item.price)}</div>
                </div>
                <div style="font-weight: 700; color: var(--primary);">
                    ${formatVND(itemTotal)}
                </div>
            </div>
        `;
    }).join("");

    updatePrices();
}

function updatePrices() {
    $("subtotal").textContent = formatVND(subtotal);

    const discountAmount = Math.round(subtotal * appliedDiscount / 100);
    const total = subtotal - discountAmount;

    if (appliedDiscount > 0) {
        $("discountLine").style.display = "flex";
        $("discountAmount").textContent = `-${formatVND(discountAmount)} (${appliedDiscount}%)`;
    } else {
        $("discountLine").style.display = "none";
    }

    $("totalPrice").textContent = formatVND(total);
    $("transferAmount").textContent = formatVND(total);
    $("transferContent").textContent = orderId;

    // Cập nhật QR Code động (VietQR - Miễn phí)
    const qrImg = document.querySelector(".qr-container img");
    if (qrImg) {
        const bankId = "MB"; // Ngân hàng Quân đội
        const accountNo = "3399377355";
        const template = "compact2";
        const amount = total;
        const description = encodeURIComponent(orderId);
        const accountName = encodeURIComponent("NGUYEN TRI THIEN");

        qrImg.src = `https://img.vietqr.io/image/${bankId}-${accountNo}-${template}.png?amount=${amount}&addInfo=${description}&accountName=${accountName}`;
    }
}

function applyDiscount() {
    const messageEl = $("discountMessage");

    // Check if logged in first
    if (!isLoggedIn) {
        messageEl.innerHTML = '<span style="color: var(--danger);">⛔ Vui lòng đăng nhập để sử dụng mã giảm giá!</span>';
        return;
    }

    const code = $("discountCode").value.trim().toUpperCase();
    console.log("DEBUG: Applying code:", code);
    console.log("DEBUG: Current User:", currentUser);

    if (!code) {
        messageEl.innerHTML = '<span style="color: var(--danger);">Vui lòng nhập mã giảm giá!</span>';
        return;
    }

    // Special code for Admin
    if (code === 'ADMIN_FREE') {
        const userRole = (currentUser && currentUser.role) ? currentUser.role.toUpperCase() : "";
        console.log("DEBUG: User Role for ADMIN_FREE:", userRole);
        if (isLoggedIn && userRole === 'ADMIN') {
            appliedDiscount = 100;
            messageEl.innerHTML = `<span style="color: #6366f1; font-weight: 700;">🛡️ XÁC NHẬN ADMIN: Chế độ bán hàng 0đ đã kích hoạt!</span>`;
            updatePrices();
            return;
        } else {
            messageEl.innerHTML = `<span style="color: var(--danger);">⛔ Mã này chỉ dành riêng cho Admin! (Vai trò hiện tại: ${userRole || 'Không xác định'})</span>`;
            return;
        }
    }

    if (DISCOUNT_CODES[code]) {
        appliedDiscount = DISCOUNT_CODES[code];
        messageEl.innerHTML = `<span style="color: var(--success);">✓ Áp dụng mã "${code}" thành công! Giảm ${appliedDiscount}%</span>`;
        updatePrices();
    } else {
        messageEl.innerHTML = '<span style="color: var(--danger);">✗ Mã giảm giá không hợp lệ!</span>';
        appliedDiscount = 0;
        updatePrices();
    }
}

async function confirmPayment() {
    const cart = getCart();
    if (cart.length === 0) {
        alert('Giỏ hàng trống!');
        return;
    }

    const proofInput = $("paymentProof");
    const btnConfirm = $("btnConfirmPayment");

    // 🛡️ CHỐNG CLICK NHIỀU LẦN: Khóa nút ngay lập tức
    btnConfirm.disabled = true;
    btnConfirm.classList.add("btn-disabled");
    btnConfirm.innerHTML = "⏳ ĐANG GỬI ĐƠN HÀNG... VUI LÒNG ĐỢI";

    let proofBase64 = null;

    if (!proofInput || !proofInput.files[0]) {
        alert("Vui lòng tải lên ảnh màn hình chuyển khoản để xác thực!");
        btnConfirm.disabled = false;
        btnConfirm.classList.remove("btn-disabled");
        btnConfirm.innerHTML = "🚀 XÁC NHẬN THANH TOÁN";
        return;
    }

    try {
        const file = proofInput.files[0];
        // 📏 GIỚI HẠN DUNG LƯỢNG: Nếu ảnh > 2MB thì báo khách chọn ảnh nhẹ hơn
        if (file.size > 2 * 1024 * 1024) {
            alert("Ảnh quá nặng! Mày hãy chụp lại màn hình hoặc dùng ảnh dưới 2MB nhé (để gửi qua Ngrok cho nhanh).");
            btnConfirm.disabled = false;
            btnConfirm.classList.remove("btn-disabled");
            btnConfirm.innerHTML = "🚀 XÁC NHẬN THANH TOÁN";
            return;
        }

        // Convert image to base64
        const reader = new FileReader();
        reader.readAsDataURL(file);
        await new Promise(resolve => reader.onload = resolve);
        proofBase64 = reader.result;

        const orderData = {
            orderId: orderId,
            paymentMethod: "BANK_TRANSFER",
            totalAmount: subtotal - Math.round(subtotal * appliedDiscount / 100),
            items: cart,
            paymentProof: proofBase64,
            status: "PENDING"
        };

        const res = await fetch('api/orders', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(orderData)
        });

        if (res.ok) {
            // ✅ THÀNH CÔNG: Hiện modal và xóa giỏ
            $("orderId").textContent = orderId;
            $("successModal").classList.add("active");
            clearCart();
            // Ẩn preview cho đỡ lag
            if ($("proofPreview")) $("proofPreview").style.display = "none";
        } else {
            const result = await res.json();
            alert('Lỗi: ' + result.message);
            // Mở lại nút nếu lỗi để khách thử lại
            btnConfirm.disabled = false;
            btnConfirm.classList.remove("btn-disabled");
            btnConfirm.innerHTML = "🚀 THỬ LẠI XÁC NHẬN";
        }
    } catch (e) {
        console.error("Payment error:", e);
        alert('Lỗi kết nối! Bạn vui lòng kiểm tra mạng và thử lại.');
        btnConfirm.disabled = false;
        btnConfirm.classList.remove("btn-disabled");
        btnConfirm.innerHTML = "🚀 THỬ LẠI XÁC NHẬN";
    }
}

window.goHome = () => {
    window.location.href = 'home.html';
};

// Event listeners
document.addEventListener('DOMContentLoaded', async () => {
    // Check login status first
    await checkLoginStatus();

    renderOrderItems();

    const btnApply = $("btnApplyDiscount");
    if (btnApply) {
        btnApply.addEventListener('click', applyDiscount);
    }

    const discountInput = $("discountCode");
    if (discountInput) {
        discountInput.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') applyDiscount();
        });
    }

    // Preview proof image and enable button
    const proofInput = $("paymentProof");
    const btnConfirm = $("btnConfirmPayment");

    // Disable button by default for bank transfer
    if (btnConfirm) {
        btnConfirm.disabled = true;
        btnConfirm.classList.add("btn-disabled");
        btnConfirm.textContent = "⏳ VUI LÒNG TẢI ẢNH CHUYỂN KHOẢN";
    }

    if (proofInput) {
        proofInput.addEventListener('change', function () {
            const file = this.files[0];
            if (file) {
                // Enable button
                btnConfirm.disabled = false;
                btnConfirm.classList.remove("btn-disabled");
                btnConfirm.textContent = "🚀 XÁC NHẬN THANH TOÁN";

                const reader = new FileReader();
                reader.onload = function (e) {
                    const preview = $("proofPreview");
                    preview.style.display = "block";
                    preview.querySelector("img").src = e.target.result;
                };
                reader.readAsDataURL(file);
            }
        });
    }

    $("btnConfirmPayment").addEventListener('click', confirmPayment);
});
