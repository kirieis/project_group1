# 🚀 REAL-TIME SIMULATOR - Hướng Dẫn Sử Dụng

## 📋 Mục Đích

Simulator là một thành phần phần mềm độc lập được thiết kế để **mô phỏng hệ thống vận hành liên tục (real-time)**. Thay vì chỉ dựa vào các thao tác bấm tay thủ công khi demo, simulator sẽ **tự động phát sinh dữ liệu** theo các kịch bản được định nghĩa.

## 🎯 Tính Năng Chính

Mỗi **10 giây** (configurable), simulator thực hiện **1 trong 3 hành động ngẫu nhiên**:

### 1️⃣ CREATE_ORDER - Tạo Đơn Hàng Mới
- **Hành động**: Gửi HTTP POST request tới `/api/orders` endpoint
- **Mô phỏng**: Khách hàng đặt hàng ngẫu nhiên
- **Chi tiết**: 
  - Tạo 1-5 items (sản phẩm)
  - Chọn thuốc ngẫu nhiên từ database
  - Số lượng: 1-10 viên/item
  - Tính tổng tiền tự động

### 2️⃣ UPDATE_INVENTORY - Cập Nhật Tồn Kho
- **Hành động**: Cập nhật số lượng tồn kho của thuốc ngẫu nhiên **trực tiếp tới database**
- **Mô phỏng**: 
  - Admin cập nhật kho hàng
  - Hoặc khách hàng mua hàng (giảm kho)
- **Chi tiết**: 
  - Chọn thuốc ngẫu nhiên
  - Cập nhật quantity: 50-500 (configurable)

### 3️⃣ ADD_MEDICINE - Thêm Sản Phẩm Mới
- **Hành động**: Thêm sản phẩm mới vào database
- **Mô phỏng**: Nhà cung cấp thêm sản phẩm mới
- **Chi tiết**:
  - Tạo medicine ID unique (SIM + timestamp)
  - Sử dụng template từ danh sách preset medicines
  - Các thông tin: name, price, quantity, dosage form, manufacturer

## 📊 Lợi Ích

✅ **Tạo dữ liệu biến động liên tục** - Hệ thống luôn có dữ liệu mới  
✅ **Test tính ổn định** - Kiểm tra code có xử lý được concurrent access không  
✅ **Phản ánh thực tế** - Giống hoạt động của hệ thống thực (Shopee, Tiki, Long Châu)  
✅ **Automated Testing** - Chạy song song với thao tác demo thủ công  
✅ **Phát sinh lỗi tiềm ẩn** - Kích hoạt xung đột dữ liệu, race conditions, deadlock...  

## 🔧 Cài Đặt & Chạy

### 1. Build Project
```bash
mvn clean compile
```

### 2. Cách Chạy (3 cách tuỳ chọn)

#### **Cách 1: Sử dụng Batch File (Windows)**
```bash
run-simulator.bat
```

#### **Cách 2: Sử dụng Maven Command**
```bash
mvn exec:java -Dexec.mainClass="simulator.Simulator"
```

#### **Cách 3: Chạy từ IDE (Eclipse/IntelliJ)**
1. Mở file `src/simulator/Simulator.java`
2. Click chuột phải → Run As → Java Application
3. Hoặc nhấn Ctrl+F11 (Eclipse) / Shift+F10 (IntelliJ)

### 3. Chuẩn Bị Database
Đảm bảo database server đang chạy:
- **Server**: `localhost:1433` (SQL Server)
- **Database**: `PharmacyDB`
- **User**: `sa` / **Pass**: `123456`

**Hoặc** tuỳ chỉnh trong [DBConnection.java](src/core_app/util/DBConnection.java)

### 4. Chuẩn Bị Web Server
Simulator tạo POST requests tới HTTP endpoint:
- **Default**: `http://localhost:8080/api/orders`
- **Cấu hình**: Xem mục **Tùy Chỉnh Cấu Hình** dưới đây

## ⚙️ Tùy Chỉnh Cấu Hình

Tôi đã tạo file cấu hình **`simulator.properties`** ở thư mục root để bạn tuỳ chỉnh mà **không cần recompile code**:

```properties
# Tần suất thực hiện hành động (milliseconds)
simulator.interval.ms=1000

# Endpoint để gửi đơn hàng
simulator.endpoint=http://localhost:8080/api/orders

# Bật/Tắt log
simulator.enable.log=true

# Bật/Tắt từng loại hành động
simulator.enable.order=true
simulator.enable.inventory=true
simulator.enable.medicine=true

# Cấu hình cho update inventory
simulator.inventory.min.quantity=50
simulator.inventory.max.quantity=100

# Cấu hình cho add medicine
simulator.medicine.initial.quantity=100
```

**Ví dụ tuỳ chỉnh:**
```properties
# Chỉ tạo order mỗi 5 giây, không update inventory hay add medicine
simulator.interval.ms=5000
simulator.enable.inventory=false
simulator.enable.medicine=false
```

## 📝 Ví Dụ Output

Khi simulator chạy, bạn sẽ thấy output như sau:

```
════════════════════════════════════════════════════════════
   SIMULATOR CONFIGURATION
════════════════════════════════════════════════════════════
  Interval: 10000ms
  Endpoint: http://localhost:8080/api/orders
  Enable Order Simulation: true
  Enable Inventory Update: true
  Enable Medicine Addition: true
  Min Quantity: 50
  Max Quantity: 500
════════════════════════════════════════════════════════════

═══════════════════════════════════════════════════════════
   🚀 REAL-TIME SIMULATOR - AUTO DATA GENERATION
═══════════════════════════════════════════════════════════

[20:57:53] Tạo đơn hàng mới | Duration: 234ms | Total - Orders: 1, Updates: 0, Medicines: 0
[20:58:03] Cập nhật số lượng tồn kho | Duration: 45ms | Total - Orders: 1, Updates: 1, Medicines: 0
[20:58:13] Thêm sản phẩm mới | Duration: 78ms | Total - Orders: 1, Updates: 1, Medicines: 1
[20:58:23] Tạo đơn hàng mới | Duration: 189ms | Total - Orders: 2, Updates: 1, Medicines: 1
...
```

## 🧪 Test Scenarios

### Scenario 1: Chỉ Tạo Order (Mô phỏng E-commerce)
```properties
simulator.enable.order=true
simulator.enable.inventory=false
simulator.enable.medicine=false
simulator.interval.ms=5000  # Mỗi 5 giây tạo 1 order
```

### Scenario 2: Cập Nhật Kho (Mô phỏng Warehouse)
```properties
simulator.enable.order=false
simulator.enable.inventory=true
simulator.enable.medicine=false
simulator.interval.ms=3000   # Mỗi 3 giây update 1 lần
``` 

### Scenario 3: Stress Test (Tất Cả Hành Động)
```properties
simulator.enable.order=true
simulator.enable.inventory=true
simulator.enable.medicine=true
simulator.interval.ms=3000   # Mỗi 3 giây 1 action → gấp 3-4x bình thường
```

### Scenario 4: Đạo Tạo Realtime (Gần Giống Thực Tế)
```properties
simulator.enable.order=true
simulator.enable.inventory=true
simulator.enable.medicine=true
simulator.interval.ms=10000  # Mỗi 10 giây 1 action
simulator.enable.log=true    # Bật log để quan sát
```

## 🔍 Monitoring Database

Khi simulator đang chạy, bạn có thể monitor database để thấy dữ liệu được tạo/cập nhật:

### Xem các đơn hàng mới
```sql
SELECT TOP 20 * FROM Invoice ORDER BY invoice_date DESC;
```

### Xem thay đổi tồn kho
```sql
SELECT medicine_id, name, quantity, price FROM Medicine ORDER BY medicine_id;
```

### Xem sản phẩm mới (tạo bởi simulator)
```sql
SELECT medicine_id, name, manufacturer FROM Medicine WHERE medicine_id LIKE 'SIM%';
```

### Xem chi tiết các item trong invoice
```sql
SELECT i.invoice_id, id.medicine_name, id.quantity, id.unit_price 
FROM Invoice i 
JOIN Invoice_Detail id ON i.invoice_id = id.invoice_id 
ORDER BY i.invoice_id DESC;
```

## 🛑 Dừng Simulator

### Cách 1: Nhấn Ctrl+C trong terminal/command prompt
```
^C
```

### Cách 2: Tắt cửa sổ terminal
Simulator sẽ in ra tóm tắt:
```
════════════════════════════════════════════════════════════
   📊 SIMULATOR SUMMARY
════════════════════════════════════════════════════════════
  Orders Created: 42
  Inventory Updates: 38
  Medicines Added: 35
  Total Actions: 115
════════════════════════════════════════════════════════════
```

## 🐛 Troubleshooting

### ❌ `Connection refused` - Không kết nối được server
**Nguyên nhân**: Web server (Tomcat) chưa chạy  
**Giải pháp**:
1. Chắc chắn Tomcat đang chạy trên port 8080
2. Hoặc cập nhật endpoint trong `simulator.properties`:
```properties
simulator.endpoint=http://localhost:9000/api/orders
```

### ❌ `SQLException` - Lỗi kết nối database
**Nguyên nhân**: SQL Server chưa chạy hoặc cấu hình sai  
**Giải pháp**:
1. Kiểm tra SQL Server đang chạy trên `localhost:1433`
2. Kiểm tra database `PharmacyDB` tồn tại
3. Cập nhật cấu hình DBConnection.java nếu cần

### ❌ `FileNotFoundException` - simulator.properties không tìm thấy
**Nguyên nhân**: File cấu hình không tồn tại hoặc vị trí sai  
**Giải pháp**:
1. Đảm bảo `simulator.properties` nằm ở thư mục root project
2. Nếu không, simulator sẽ sử dụng default config (vẫn chạy được, chỉ là output cứu)

## 📚 Tài Liệu Thêm

| File | Mục Đích |
|------|---------|
| [Simulator.java](src/simulator/Simulator.java) | Main class - Logic simulator |
| [SimulationAction.java](src/simulator/SimulationAction.java) | Enum các hành động |
| [SimulatorConfig.java](src/simulator/SimulatorConfig.java) | Quản lý cấu hình |
| [MedicineDAO.java](src/core_app/dao/MedicineDAO.java) | DAO để update/add medicine |
| [OrderServlet.java](src/core_app/servlet/OrderServlet.java) | Endpoint nhận order |
| [simulator.properties](simulator.properties) | File cấu hình |

## 💡 Một Số Tips

1. **Monitor Real-time**: Mở 2 terminal cùng lúc:
   - Terminal 1: Chạy simulator
   - Terminal 2: Query database để thấy thay đổi
   ```bash
   cd path\to\project
   sqlcmd -S localhost -U sa -P 123456
   USE PharmacyDB;
   SELECT COUNT(*) FROM Invoice;  -- Chạy lặp lại mỗi 10 giây
   ```

2. **Test Concurrent Access**: Khi simulator chạy, mở browser:
   - Đăng nhập vào hệ thống
   - Đặt hàng thủ công **cùng lúc** simulator đang chạy
   - Quan sát nếu có lỗi hoặc data inconsistency

3. **Long Running Test**: Cấu hình simulator để chạy 1-2 tiếng:
   ```properties
   simulator.interval.ms=10000
   simulator.enable.log=true  # Để quan sát liên tục
   ```
   Sau đó check database xem có vấn đề gì không.

## 📞 Liên Hệ & Hỗ Trợ

Nếu có vấn đề, hãy:
1. Kiểm tra log output của simulator
2. Check database logs
3. Kiểm tra cấu hình trong `simulator.properties`
4. Xem các error message và search Google/ChatGPT

---

**Tạo bởi**: Simulator Auto Data Generation System  
**Version**: 1.0  
**Last Updated**: 2026-03-03
