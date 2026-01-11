package com.longchau.pharmacy.config;

import com.github.javafaker.Faker;
import com.longchau.pharmacy.entity.*; // Import các Entity đã viết ở bài trước
import com.longchau.pharmacy.repository.*; // Import các Repository tương ứng
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Component
public class DataSeeder implements CommandLineRunner {

    // Khai báo các Repository để lưu dữ liệu
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    private final Faker faker;
    private final Random random;

    // Constructor Injection
    public DataSeeder(CategoryRepository categoryRepository, ProductRepository productRepository,
                      ProductVariantRepository variantRepository, UserRepository userRepository,
                      OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        
        // Cấu hình Faker tiếng Việt để dữ liệu nhìn thân thiện
        this.faker = new Faker(new Locale("vi"));
        this.random = new Random();
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Kiểm tra nếu DB đã có dữ liệu thì không chạy lại (tránh trùng lặp)
        if (categoryRepository.count() > 0) {
            System.out.println("Dữ liệu đã tồn tại, bỏ qua bước Seeding.");
            return;
        }

        System.out.println("Đang bắt đầu sinh 10.000+ dòng dữ liệu mẫu...");
        long startTime = System.currentTimeMillis();

        // 1. Tạo Danh mục (Categories) - Khoảng 10 cái
        List<Category> categories = generateCategories();
        
        // 2. Tạo Sản phẩm (Products) & Biến thể (Variants) - Khoảng 1.000 thuốc
        List<ProductVariant> allVariants = generateProductsAndVariants(categories, 1000);
        
        // 3. Tạo User (Khách hàng) - Khoảng 500 người
        List<User> users = generateUsers(500);
        
        // 4. Tạo Đơn hàng (Orders) - Khoảng 2.000 đơn
        // Mỗi đơn có trung bình 3 sản phẩm -> Tổng cộng ~6.000 dòng chi tiết đơn hàng
        generateOrders(users, allVariants, 2000);

        long endTime = System.currentTimeMillis();
        System.out.println("Hoàn tất! Tổng thời gian: " + (endTime - startTime) + "ms");
        System.out.println("Tổng dữ liệu đã sinh ra:");
        System.out.println("- Categories: " + categoryRepository.count());
        System.out.println("- Products: " + productRepository.count());
        System.out.println("- Variants (SKUs): " + variantRepository.count());
        System.out.println("- Users: " + userRepository.count());
        System.out.println("- Orders: " + orderRepository.count());
        System.out.println("- Order Items: " + orderItemRepository.count());
    }

    // --- CÁC HÀM HỖ TRỢ SINH DỮ LIỆU ---

    private List<Category> generateCategories() {
        List<Category> categories = new ArrayList<>();
        String[] catNames = {
            "Thuốc Thần Kinh", "Thực phẩm chức năng", "Dược mỹ phẩm", 
            "Thiết bị y tế", "Chăm sóc cá nhân", "Vitamin & Khoáng chất", 
            "Thuốc Tim Mạch", "Thuốc Tiêu Hóa", "Mẹ và Bé", "Thuốc Kháng Sinh"
        };

        for (String name : catNames) {
            Category c = new Category();
            c.setName(name);
            c.setSlug(faker.slugify().slugify(name));
            c.setDescription(faker.lorem().sentence());
            categories.add(c);
        }
        return categoryRepository.saveAll(categories);
    }

    private List<ProductVariant> generateProductsAndVariants(List<Category> categories, int count) {
        List<Product> products = new ArrayList<>();
        List<ProductVariant> variants = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            Product p = new Product();
            // Tự tạo tên thuốc cho giống thật hơn vì Faker ít tên thuốc VN
            String productName = faker.medical().medicineName() + " " + faker.number().digits(3) + "mg";
            
            p.setName(productName);
            p.setSlug(faker.slugify().slugify(productName) + "-" + i);
            p.setBrand(faker.company().name());
            p.setOrigin(faker.country().name());
            p.setDescription(faker.lorem().paragraph());
            p.setIngredients(faker.lorem().sentence(10)); // Thành phần giả
            p.setUsageInstruction("Uống sau khi ăn, ngày " + (random.nextInt(3) + 1) + " lần.");
            p.setPrescriptionRequired(random.nextBoolean()); // Random thuốc kê đơn
            p.setCategory(categories.get(random.nextInt(categories.size()))); // Random danh mục
            p.setImageUrl("https://dummyimage.com/300x300/000/fff&text=" + productName.substring(0,3));
            
            products.add(p);
        }
        productRepository.saveAll(products);

        // Với mỗi thuốc, tạo các đơn vị bán (Hộp, Vỉ, Viên)
        for (Product p : products) {
            // Variant 1: Hộp (Luôn có)
            ProductVariant vBox = new ProductVariant();
            vBox.setProduct(p);
            vBox.setUnitName("Hộp");
            double basePrice = 50000 + random.nextInt(500000); // Giá từ 50k đến 550k
            vBox.setPrice(basePrice);
            vBox.setOriginalPrice(basePrice * 1.1); // Giá gốc cao hơn 10%
            vBox.setStockQuantity(random.nextInt(100));
            vBox.setConversionFactor(1);
            variants.add(vBox);

            // Variant 2: Vỉ (Xác suất 70% có bán lẻ theo vỉ)
            if (random.nextBoolean()) {
                ProductVariant vBlister = new ProductVariant();
                vBlister.setProduct(p);
                vBlister.setUnitName("Vỉ");
                vBlister.setPrice(basePrice / 10 * 1.2); // Đắt hơn chút nếu mua lẻ
                vBlister.setStockQuantity(random.nextInt(200));
                vBlister.setConversionFactor(10); // 1 Hộp = 10 Vỉ
                variants.add(vBlister);
            }
        }
        return variantRepository.saveAll(variants);
    }

    private List<User> generateUsers(int count) {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            User u = new User();
            u.setFullName(faker.name().fullName());
            u.setEmail(faker.internet().emailAddress());
            u.setPhoneNumber(faker.phoneNumber().cellPhone());
            u.setPasswordHash("hashed_password_123"); // Giả lập mật khẩu đã mã hóa
            u.setAddress(faker.address().fullAddress());
            
            // 90% là khách hàng, 10% là dược sĩ
            u.setRole(random.nextInt(10) == 0 ? Role.PHARMACIST : Role.CUSTOMER);
            
            users.add(u);
        }
        return userRepository.saveAll(users);
    }

    private void generateOrders(List<User> users, List<ProductVariant> variants, int count) {
        List<Order> orders = new ArrayList<>();
        List<OrderItem> orderItems = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            Order o = new Order();
            User user = users.get(random.nextInt(users.size()));
            o.setUser(user);
            o.setShippingAddress(user.getAddress());
            o.setShippingPhone(user.getPhoneNumber());
            
            // Random trạng thái đơn hàng
            OrderStatus[] statuses = OrderStatus.values();
            o.setStatus(statuses[random.nextInt(statuses.length)]);
            
            o.setPaymentMethod(PaymentMethod.COD);
            o.setCreatedAt(convertToLocalDateTime(faker.date().past(365, TimeUnit.DAYS))); // Đơn hàng trong 1 năm qua
            
            // Lưu đơn hàng trước để có ID
            orders.add(o);
        }
        // Lưu Batch đợt 1 để lấy ID
        orders = orderRepository.saveAll(orders);

        // Tạo chi tiết đơn hàng (Order Items)
        for (Order o : orders) {
            double totalAmount = 0;
            int numberOfItems = random.nextInt(4) + 1; // Mỗi đơn mua 1-4 loại thuốc

            for (int k = 0; k < numberOfItems; k++) {
                OrderItem item = new OrderItem();
                item.setOrder(o);
                
                ProductVariant variant = variants.get(random.nextInt(variants.size()));
                item.setProductVariant(variant);
                
                int quantity = random.nextInt(3) + 1;
                item.setQuantity(quantity);
                item.setUnitPrice(variant.getPrice());
                
                totalAmount += (variant.getPrice() * quantity);
                
                orderItems.add(item);
            }
            o.setTotalAmount(totalAmount);
        }
        
        // Lưu toàn bộ items và cập nhật lại totalAmount cho Order
        orderItemRepository.saveAll(orderItems);
        orderRepository.saveAll(orders);
    }
    
    // Helper chuyển Date sang LocalDateTime
    private LocalDateTime convertToLocalDateTime(java.util.Date dateToConvert) {
        return dateToConvert.toInstant()
          .atZone(ZoneId.systemDefault())
          .toLocalDateTime();
    }
}