/**
 * Centralized Internationalization Dictionary
 * Supports Page-specific and Common translations
 */

// Current language state
let currentLang = localStorage.getItem("lang") || "vi";

const i18n_common = {
    vi: {
        nav_home: "Trang chủ",
        nav_cart: "Giỏ hàng",
        nav_profile: "Hồ sơ",
        nav_login: "Đăng nhập",
        nav_logout: "Đăng xuất",
        nav_help: "Trợ giúp",
        btn_back_home: "Quay lại trang chủ",
        lbl_search: "Tìm kiếm sản phẩm...",
        lbl_code: "Mã",
        lbl_type: "Loại",
        unit_pill: "viên",
        unit_strip: "vỉ",
        unit_box: "hộp",
        unit_bottle: "chai",
        unit_setting: "Đơn vị tính",
        quantity: "Số lượng",
        cancel: "Hủy",
        add_to_cart: "Thêm vào giỏ",
        buy_now: "Mua ngay",
        footer_connect: "Kết nối với chúng tôi : 03xxxxxxx"
    },
    en: {
        nav_home: "Home",
        nav_cart: "Cart",
        nav_profile: "Profile",
        nav_login: "Login",
        nav_logout: "Logout",
        nav_help: "Help",
        btn_back_home: "Back to Home",
        lbl_search: "Search medicines...",
        lbl_code: "Code",
        lbl_type: "Type",
        unit_pill: "pill",
        unit_strip: "strip",
        unit_box: "box",
        unit_bottle: "bottle",
        unit_setting: "Unit",
        quantity: "Quantity",
        cancel: "Cancel",
        add_to_cart: "Add to Cart",
        buy_now: "Buy Now",
        footer_connect: "Connect with us : 03xxxxxxx"
    }
};

const i18n_pages = {
    home: {
        vi: {
            hero_title: "Mua thuốc nhanh – tìm dễ – lọc chuẩn",
            hero_subtitle: "Cập nhật trực tiếp từ hệ thống quản lý kho.",
            hero_btn_sale: "Xem Sale 🔥",
            hero_btn_all: "Xem toàn bộ",
            hero_tag: "TƯ VẤN VIÊN",
            hero_quote: "Tư vấn tận tâm",
            section_sale: "Đang Sale",
            section_sale_sub: "Các sản phẩm giảm giá hấp dẫn",
            section_best: "Bán chạy nhất",
            section_best_sub: "Sản phẩm được khách hàng tin dùng",
            section_all: "Tất cả sản phẩm",
            filter_title: "Bộ lọc",
            filter_keywords: "Từ khóa",
            filter_price_min: "Giá thấp nhất",
            filter_price_max: "Giá cao nhất",
            filter_sort: "Sắp xếp",
            filter_sale_only: "Chỉ hiện Sale",
            filter_reset: "Xóa lọc",
            filter_apply: "Áp dụng",
            sort_pop: "Phổ biến nhất",
            sort_price_asc: "Giá: Thấp đến Cao",
            sort_price_desc: "Giá: Cao đến Thấp",
            sort_newest: "Mới nhất",
            sort_name_asc: "Tên A-Z",
            results_count: "kết quả",
            empty_msg: "Không tìm thấy sản phẩm.",
            empty_filter: "Không có sản phẩm phù hợp bộ lọc.",
            page_info: "Trang {now} / {total}",
            btn_prev: "← Trước",
            btn_next: "Sau →",
            toast_added: "đã được thêm vào giỏ!",
            card_code: "Mã",
            card_type: "Loại",
            card_exp: "HSD",
            card_buy: "MUA",
            loading: "Đang tải sản phẩm..."
        },
        en: {
            hero_title: "Fast Purchase – Easy Search – Pro Filter",
            hero_subtitle: "Real-time updates from inventory management.",
            hero_btn_sale: "View Sales 🔥",
            hero_btn_all: "Browse All",
            hero_tag: "PHARMACIST",
            hero_quote: "Dedicated Advice",
            section_sale: "On Sale",
            section_sale_sub: "Hot deals on premium medicines",
            section_best: "Best Sellers",
            section_best_sub: "Most popular picks this week",
            section_all: "All Products",
            filter_title: "Filters",
            filter_keywords: "Keywords",
            filter_price_min: "Min Price",
            filter_price_max: "Max Price",
            filter_sort: "Sort By",
            filter_sale_only: "Sale Only",
            filter_reset: "Reset",
            filter_apply: "Apply",
            sort_pop: "Most Popular",
            sort_price_asc: "Price: Low to High",
            sort_price_desc: "Price: High to Low",
            sort_newest: "Newest Arrivals",
            sort_name_asc: "Name: A-Z",
            results_count: "results",
            empty_msg: "No products found.",
            empty_filter: "No products match your filters.",
            page_info: "Page {now} / {total}",
            btn_prev: "← Prev",
            btn_next: "Next →",
            toast_added: "has been added to cart!",
            card_code: "Code",
            card_type: "Type",
            card_exp: "Exp",
            card_buy: "BUY",
            loading: "Loading products..."
        }
    },
    login: {
        vi: {
            title: "Đăng nhập",
            subtitle: "Chào mừng trở lại Github Pharmacy",
            lbl_user: "Tên đăng nhập",
            lbl_pass: "Mật khẩu",
            btn_login: "Đăng Nhập",
            no_account: "Chưa có tài khoản?",
            link_register: "Đăng ký ngay",
            msg_success: "🎉 Đăng ký thành công! Hãy đăng nhập ngay.",
            msg_unauthorized: "⛔ Bạn không có quyền truy cập trang này!",
            msg_error: "❌ Sai tên đăng nhập hoặc mật khẩu!"
        },
        en: {
            title: "Login",
            subtitle: "Welcome back to Github Pharmacy",
            lbl_user: "Username",
            lbl_pass: "Password",
            btn_login: "Sign In",
            no_account: "Don't have an account?",
            link_register: "Register now",
            msg_success: "🎉 Registration successful! Please login.",
            msg_unauthorized: "⛔ You don't have permission to access this page!",
            msg_error: "❌ Invalid username or password!"
        }
    },
    register: {
        vi: {
            title: "Tạo tài khoản",
            subtitle: "Tham gia cộng đồng Github Pharmacy ngay",
            lbl_fullname: "Họ và tên",
            lbl_phone: "Số điện thoại",
            lbl_email: "Email (Để nhận mã OTP)",
            lbl_address: "Địa chỉ",
            lbl_user: "Tên đăng nhập",
            lbl_pass: "Mật khẩu",
            rule_len: "Ít nhất 8 ký tự",
            rule_upper: "Có chữ IN HOA",
            rule_lower: "Có chữ thường",
            rule_num: "Có con số",
            btn_register: "Đăng Ký",
            has_account: "Đã có tài khoản?",
            link_login: "Đăng nhập ngay",
            msg_weak_pass: "❌ Mật khẩu không đạt yêu cầu bảo mật!",
            msg_exists: "❌ Tên đăng nhập đã tồn tại!",
            msg_error: "❌ Đăng ký thất bại. Vui lòng thử lại!"
        },
        en: {
            title: "Create Account",
            subtitle: "Join the Github Pharmacy community today",
            lbl_fullname: "Full Name",
            lbl_phone: "Phone Number",
            lbl_email: "Email (For OTP codes)",
            lbl_address: "Address",
            lbl_user: "Username",
            lbl_pass: "Password",
            rule_len: "At least 8 characters",
            rule_upper: "UPPERCASE letter",
            rule_lower: "lowercase letter",
            rule_num: "A number",
            btn_register: "Sign Up",
            has_account: "Already have an account?",
            link_login: "Login here",
            msg_weak_pass: "❌ Password does not meet security requirements!",
            msg_exists: "❌ Username already exists!",
            msg_error: "❌ Registration failed. Please try again!"
        }
    },
    verify_otp: {
        vi: {
            title: "Xác thực Email",
            subtitle: "Mã OTP đã được gửi tới email của bạn. Vui lòng kiểm tra và nhập mã bên dưới.",
            error_invalid: "Mã OTP không chính xác!",
            btn_verify: "Xác nhận đăng ký",
            link_back: "Quay lại trang đăng ký"
        },
        en: {
            title: "Email Verification",
            subtitle: "An OTP has been sent to your email. Please check and enter the code below.",
            error_invalid: "Invalid OTP code!",
            btn_verify: "Verify Registration",
            link_back: "Back to registration"
        }
    },
    cart: {
        vi: {
            title: "Giỏ hàng của bạn",
            btn_continue: "Tiếp tục mua sắm",
            summary_title: "Tổng quát đơn hàng",
            subtotal: "Tạm tính",
            shipping: "Phí vận chuyển",
            shipping_free: "Miễn phí",
            total: "Tổng cộng",
            btn_checkout: "XÁC NHẬN THANH TOÁN",
            empty: "Giỏ hàng của bạn đang trống",
            loading: "Đang tải giỏ hàng..."
        },
        en: {
            title: "Your Shopping Cart",
            btn_continue: "Continue Shopping",
            summary_title: "Order Summary",
            subtotal: "Subtotal",
            shipping: "Shipping",
            shipping_free: "Free",
            total: "Total",
            btn_checkout: "CHECKOUT NOW",
            empty: "Your cart is currently empty",
            loading: "Loading your cart..."
        }
    },
    profile: {
        vi: {
            tab_overview: "Tổng quan",
            tab_history: "Lịch sử mua hàng",
            tab_info: "Thông tin cá nhân",
            stat_orders: "Đơn hàng đã mua",
            stat_spent: "Tổng tiền tích lũy",
            recent_orders: "Đơn hàng gần đây",
            no_recent: "Chưa có đơn hàng nào gần đây.",
            no_orders: "Bạn chưa có đơn hàng nào.",
            lbl_name: "Họ và tên",
            lbl_phone: "Số điện thoại",
            lbl_address: "Địa chỉ",
            lbl_user: "Tên đăng nhập",
            role_admin: "Quản trị viên",
            role_member: "Thành viên",
            order_id: "Đơn hàng",
            order_status_pending: "⏳ Chờ xác nhận",
            order_status_paid: "✅ Đã hoàn tất",
            order_status_cancelled: "❌ Đã bị hủy",
            order_wait_pay: "💡 Đơn hàng đang chờ thanh toán qua ngân hàng.",
            btn_pay_qr: "💳 Thanh toán / Xem mã QR",
            btn_cancel: "✕ Hủy đơn",
            not_updated: "Chưa cập nhật"
        },
        en: {
            tab_overview: "Overview",
            tab_history: "Purchase History",
            tab_info: "Personal Info",
            stat_orders: "Total Orders",
            stat_spent: "Total Accumulated",
            recent_orders: "Recent Orders",
            no_recent: "No recent orders found.",
            no_orders: "You have no orders yet.",
            lbl_name: "Full Name",
            lbl_phone: "Phone Number",
            lbl_address: "Address",
            lbl_user: "Username",
            role_admin: "Administrator",
            role_member: "Member",
            order_id: "Order",
            order_status_pending: "⏳ Pending",
            order_status_paid: "✅ Completed",
            order_status_cancelled: "❌ Cancelled",
            order_wait_pay: "💡 Order is awaiting bank transfer payment.",
            btn_pay_qr: "💳 Pay / View QR",
            btn_cancel: "✕ Cancel Order",
            not_updated: "Not updated"
        }
    },
    admin: {
        vi: {
            user_title: "Quản lý người dùng",
            sales_title: "Doanh thu & Tiền lời",
            nav_dashboard: "Bảng điều khiển",
            nav_users: "Người dùng",
            nav_orders: "Đơn hàng",
            nav_products: "Sản phẩm",
            stat_total_sales: "Tổng doanh thu",
            stat_orders: "Tổng đơn hàng",
            stat_profit: "Lợi nhuận gộp",
            stat_active_users: "Khách hàng mới",
            lbl_orders: "ĐƠN ĐÃ TRẢ",
            lbl_revenue: "DOANH THU",
            lbl_profit: "LỢI NHUẬN GỘP",
            lbl_recent: "Giao dịch gần đây",
            lbl_search_placeholder: "Tìm theo tên hoặc ID...",
            btn_refresh: "Làm mới",
            header_id: "Mã đơn",
            header_date: "Ngày",
            header_buyer: "Người mua",
            header_method: "PTTT",
            header_total: "Tổng tiền",
            header_profit: "Lợi nhuận",
            header_status: "Trạng thái",
            header_action: "Hành động",
            status_paid: "ĐÃ TRẢ",
            status_cancelled: "ĐÃ HỦY",
            status_pending: "CHỜ DUYỆT",
            lbl_items_in_order: "Sản phẩm trong đơn",
            lbl_gross_profit: "Lợi nhuận gộp",
            lbl_payment_proof: "Ảnh bằng chứng",
            btn_reject: "Hủy/Từ chối",
            btn_approve: "Duyệt & Xác nhận",
            msg_confirm_approve: "Xác nhận đơn hàng này đã được thanh toán?",
            msg_update_success: "Cập nhật trạng thái thành công!",
            msg_update_error: "Cập nhật thất bại!",
            lbl_create_acc: "Tạo tài khoản mới",
            lbl_fullname: "Họ và tên",
            lbl_user: "Tên đăng nhập",
            lbl_pass: "Mật khẩu",
            lbl_access_level: "Quyền truy cập",
            lbl_customer: "KHÁCH HÀNG",
            lbl_admin: "QUẢN TRỊ VIÊN",
            lbl_min_chars: "Ít nhất 8 ký tự",
            lbl_upper_case: "Có chữ HOA",
            lbl_lower_case: "Có chữ thường",
            lbl_numbers: "Có con số",
            btn_create_acc: "Tạo tài khoản",
            lbl_account_directory: "Danh sách tài khoản",
            lbl_search_accounts_placeholder: "Tìm tài khoản...",
            header_profile: "Thông tin",
            header_role: "Quyền",
            header_operations: "Thao tác",
            btn_demote: "Thu hồi quyền Admin",
            btn_promote: "Cấp quyền Admin",
            btn_delete: "Xóa người dùng",
            msg_confirm_delete: "Hành động này không thể hoàn tác. Bạn chắc chứ?",
            msg_confirm_promote: "Bạn có chắc muốn cấp quyền Admin cho người dùng này?",
            msg_confirm_demote: "Bạn có chắc muốn thu hồi quyền Admin của người dùng này?",
            msg_fill_info: "Vui lòng điền đủ thông tin!",
            msg_weak_pass: "Mật khẩu chưa đạt yêu cầu bảo mật!",
            msg_account_exists: "Tên đăng nhập đã tồn tại hoặc có lỗi!",
            msg_delete_success: "Đã xóa người dùng thành công.",
            msg_delete_error: "Lỗi khi xóa người dùng!",
            nav_website: "Xem website",
            role_admin_badge: "🛡️ Admin",
            role_member_badge: "👤 Thành viên",
            guest_customer: "Khách vãng lai"
        },
        en: {
            user_title: "User Management",
            sales_title: "Revenue & Profit",
            nav_dashboard: "Dashboard",
            nav_users: "Users",
            nav_orders: "Orders",
            nav_products: "Products",
            stat_total_sales: "Total Revenue",
            stat_orders: "Total Orders",
            stat_profit: "Gross Profit",
            stat_active_users: "New Customers",
            lbl_orders: "PAID ORDERS",
            lbl_revenue: "REVENUE",
            lbl_profit: "EST. PROFIT",
            lbl_recent: "Recent Transactions",
            lbl_search_placeholder: "Search by buyer or ID...",
            btn_refresh: "Refresh",
            header_id: "Order ID",
            header_date: "Date",
            header_buyer: "Buyer",
            header_method: "Method",
            header_total: "Total",
            header_profit: "Profit",
            header_status: "Status",
            header_action: "Action",
            status_paid: "PAID",
            status_cancelled: "CANCELLED",
            status_pending: "PENDING",
            lbl_items_in_order: "Items in order",
            lbl_gross_profit: "Gross Profit",
            lbl_payment_proof: "Payment Proof",
            btn_reject: "Reject / Cancel",
            btn_approve: "Approve Payment",
            msg_confirm_approve: "Confirm this order has been paid?",
            msg_update_success: "Status updated successfully!",
            msg_update_error: "Update failed!",
            lbl_create_acc: "Create New Account",
            lbl_fullname: "Full Name",
            lbl_user: "Username",
            lbl_pass: "Password",
            lbl_access_level: "Access Level",
            lbl_customer: "CUSTOMER",
            lbl_admin: "ADMIN",
            lbl_min_chars: "Min 8 chars",
            lbl_upper_case: "Upper case",
            lbl_lower_case: "Lower case",
            lbl_numbers: "Numbers",
            btn_create_acc: "Create Account",
            lbl_account_directory: "Account Directory",
            lbl_search_accounts_placeholder: "Search accounts...",
            header_profile: "User Profile",
            header_role: "Role",
            header_operations: "Operations",
            btn_demote: "Demote from Admin",
            btn_promote: "Promote to Admin",
            btn_delete: "Delete user",
            msg_confirm_delete: "This action cannot be undone. Are you sure?",
            msg_confirm_promote: "Are you sure you want to promote this user to Admin?",
            msg_confirm_demote: "Are you sure you want to demote this user from Admin?",
            msg_fill_info: "Please fill in all fields!",
            msg_weak_pass: "Password does not meet security requirements!",
            msg_account_exists: "Username already exists or error!",
            msg_delete_success: "User deleted successfully.",
            msg_delete_error: "Error deleting user!",
            nav_website: "View Website",
            role_admin_badge: "🛡️ Admin",
            role_member_badge: "👤 Member",
            guest_customer: "Guest Customer"
        }
    },
    checkout: {
        vi: {
            title: "Thanh toán đơn hàng",
            btn_back_cart: "← Quay lại giỏ hàng",
            order_details: "📋 Chi tiết đơn hàng",
            lbl_discount: "Nhập mã giảm giá...",
            btn_apply: "Áp dụng",
            subtotal: "Tạm tính",
            discount: "Giảm giá",
            shipping: "Phí vận chuyển",
            total: "Tổng cộng",
            pay_title: "💳 Thanh toán chuyển khoản",
            pay_sepay: "Thanh toán Ngân hàng (SePay)",
            pay_desc: "Quét mã VietQR để hệ thống tự động xác nhận đơn hàng của bạn ngay lập tức.",
            pay_benefit1: "✅ Tự động điền số tiền và nội dung",
            pay_benefit2: "✅ Xử lý nhanh 24/7 (kể cả ban đêm)",
            pay_benefit3: "✅ Bảo mật tối đa qua NAPAS 247",
            btn_confirm: "🚀 XÁC NHẬN VÀ HIỆN MÃ QR",
            success_title: "Đã gửi yêu cầu thanh toán!",
            success_desc: "Bằng chứng chuyển khoản đã được gửi. Đợi Admin kiểm tra.",
            success_tip: "💡 Mẹo: Vào Hồ sơ -> Lịch sử để theo dõi nhé!",
            order_id: "Mã đơn hàng",
            btn_home: "Về Trang Chủ",
            btn_profile: "Vào Hồ Sơ 👤",
            login_to_discount: "Đăng nhập để sử dụng mã giảm giá",
            invalid_discount: "Mã không hợp lệ!",
            empty_discount: "Vui lòng nhập mã giảm giá!",
            applied_discount: "Áp dụng mã",
            initializing_order: "ĐANG KHỞI TẠO ĐƠN HÀNG...",
            qr_title: "Thanh toán QR",
            qr_desc: "Mở app ngân hàng → Quét mã QR",
            amount: "Số tiền",
            transfer_content: "Nội dung CK",
            bank: "Ngân hàng",
            waiting_payment: "Đang chờ hệ thống xác nhận giao dịch...",
            security_note: "🔒 Nếu đã chuyển khoản, hệ thống sẽ tự xác nhận. Không đóng trang này.",
            cancel_order_confirm: "Bạn có chắc muốn hủy đơn hàng?",
            order_cancelled: "Đã hủy đơn hàng",
            order_cancelled_desc: "Đơn hàng đã được hủy. Nếu đã chuyển tiền, vui lòng liên hệ Admin.",
            payment_success: "Thanh toán thành công!",
            payment_success_desc: "Đơn hàng đã được xác nhận.",
            not_received_yet: "Chưa nhận được tiền",
            not_received_yet_desc: "Hệ thống chưa nhận được giao dịch. Nếu đã chuyển, vui lòng đợi thêm.",
            cancel_btn: "Hủy đơn hàng"
        },
        en: {
            title: "Order Checkout",
            btn_back_cart: "← Back to Cart",
            order_details: "📋 Order Details",
            lbl_discount: "Enter discount code...",
            btn_apply: "Apply",
            subtotal: "Subtotal",
            discount: "Discount",
            shipping: "Shipping",
            total: "Grand Total",
            pay_title: "💳 Bank Transfer Payment",
            pay_sepay: "Bank Payment (SePay)",
            pay_desc: "Scan VietQR for automatic instant confirmation of your order.",
            pay_benefit1: "✅ Auto-filled amount & content",
            pay_benefit2: "✅ Fast 24/7 processing",
            pay_benefit3: "✅ Maximum security",
            btn_confirm: "🚀 CONFIRM & SHOW QR CODE",
            success_title: "Payment Request Sent!",
            success_desc: "Your transfer proof has been submitted. Awaiting Admin review.",
            success_tip: "💡 Tip: Check Profile -> History to track your status!",
            order_id: "Order ID",
            btn_home: "Back to Home",
            btn_profile: "View Profile 👤",
            login_to_discount: "Login to use discount code",
            invalid_discount: "Invalid code!",
            empty_discount: "Please enter a discount code!",
            applied_discount: "Applied code",
            initializing_order: "INITIALIZING ORDER...",
            qr_title: "QR Payment",
            qr_desc: "Open bank app → Scan QR",
            amount: "Amount",
            transfer_content: "Content",
            bank: "Bank",
            waiting_payment: "Waiting for system confirmation...",
            security_note: "🔒 If you have already transferred, it will be auto-confirmed. Do not close this page.",
            cancel_order_confirm: "Are you sure you want to cancel this order?",
            order_cancelled: "Order Cancelled",
            order_cancelled_desc: "Order has been cancelled. If you transferred, please contact Admin.",
            payment_success: "Payment Successful!",
            payment_success_desc: "Your order has been confirmed.",
            not_received_yet: "Payment not received",
            not_received_yet_desc: "We haven't detected your transaction yet. If you paid, please wait.",
            cancel_btn: "Cancel Order"
        }
    }
};

/**
 * Get translated string
 */
function t(page, key) {
    const pageDict = i18n_pages[page] || {};
    const commonDict = i18n_common;

    // Check page dictionary first, then common
    let result = (pageDict[currentLang] && pageDict[currentLang][key])
        || (commonDict[currentLang] && commonDict[currentLang][key]);

    // Fallback to Vietnamese if not found in English
    if (!result && currentLang === 'en') {
        result = (pageDict['vi'] && pageDict['vi'][key])
            || (commonDict['vi'] && commonDict['vi'][key]);
    }

    return result || key;
}

/**
 * Apply translations to all marked elements
 */
function translateAll() {
    console.log("Translating all elements to:", currentLang);

    // Translate text content
    const elements = document.querySelectorAll("[data-i18n]");
    elements.forEach(el => {
        const i18nAttr = el.getAttribute("data-i18n");
        const dotIndex = i18nAttr.indexOf('.');
        if (dotIndex > -1) {
            const page = i18nAttr.substring(0, dotIndex);
            const key = i18nAttr.substring(dotIndex + 1);
            el.innerHTML = t(page, key);
        } else {
            el.innerHTML = t("common", i18nAttr);
        }
    });

    // Translate placeholders
    const placeholders = document.querySelectorAll("[data-i18n-placeholder]");
    placeholders.forEach(el => {
        const i18nAttr = el.getAttribute("data-i18n-placeholder");
        const dotIndex = i18nAttr.indexOf('.');
        if (dotIndex > -1) {
            const page = i18nAttr.substring(0, dotIndex);
            const key = i18nAttr.substring(dotIndex + 1);
            el.placeholder = t(page, key);
        } else {
            el.placeholder = t("common", i18nAttr);
        }
    });

    // Update body class for language-specific styling
    document.body.classList.remove('lang-vi', 'lang-en');
    document.body.classList.add('lang-' + currentLang);
    document.documentElement.lang = currentLang;
}

/**
 * Set language and update UI
 */
function setLanguage(lang) {
    currentLang = lang;
    localStorage.setItem("lang", lang);
    translateAll();

    // Dispatch event for other scripts
    window.dispatchEvent(new CustomEvent('langChanged', { detail: lang }));
}

// Auto-translate on load
document.addEventListener("DOMContentLoaded", translateAll);
