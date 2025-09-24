# QuanLyCuaHangLapTop (Android)

Ứng dụng Android quản lý cửa hàng laptop: người dùng duyệt sản phẩm, thêm giỏ hàng, đặt hàng và theo dõi đơn; quản trị viên quản lý sản phẩm, đơn và xem báo cáo.

## 1) Mô tả BASIC
- **Nền tảng**: Android (Java), kiến trúc nhiều lớp: `UI (Activity/Fragment)` → `Service` → `Repository` → `SQLite`.
- **CSDL**: SQLite nội bộ thông qua `AppDatabase` (không backend).
- **Điều hướng**: Bottom Navigation cho người dùng; Drawer/Menu cho Admin.
- **Phân quyền**: `USER` và `ADMIN` (lưu session qua `SessionManager`).

## 2) Tính năng chính
- **Người dùng**:
  - Đăng ký/Đăng nhập, lưu phiên.
  - Duyệt danh sách sản phẩm, xem chi tiết, lọc/sắp xếp cơ bản.
  - Thêm vào giỏ, quản lý giỏ hàng.
  - Thanh toán (Checkout) và xem màn hình cảm ơn.
  - Xem lịch sử đơn hàng, xem chi tiết đơn hàng.
  - Cập nhật thông tin cá nhân.
- **Quản trị**:
  - Dashboard tổng quan.
  - Quản lý sản phẩm: thêm/sửa/xoá, xem danh sách.
  - Quản lý đơn: lọc theo trạng thái, xem/đổi trạng thái, xem chi tiết từng đơn.
  - Báo cáo: doanh thu theo năm, top sản phẩm, biểu đồ cột/thị phần (MPAndroidChart).

## 3) Luồng hoạt động
- Ứng dụng khởi động tại `SplashActivity`:
  - Nếu đã đăng nhập: điều hướng theo role → `AdminActivity` (ADMIN) hoặc `MainActivity` (USER).
  - Nếu chưa đăng nhập: chuyển tới `LoginActivity`.
- `MainActivity`: quản lý các fragment người dùng với Bottom Navigation: `HomeFragment`, `AllProductsFragment`, `CartFragment`, `ProfileFragment`.
- Mua hàng:
  - Từ `HomeFragment/AllProductsFragment` → `ProductDetailFragment` → thêm giỏ (`CartService`).
  - `CartFragment` → `CheckoutActivity` → tạo Order (`OrderService`) → `ThankYouActivity`.
- Lịch sử đơn: `OrderHistoryActivity` → lọc theo `OrderStatus` → `OrderDetailActivity`.
- Quản trị: `AdminActivity` với menu:
  - `ProductsFragment` (CRUD sản phẩm), `OrdersFragment` (danh sách/chi tiết/đổi trạng thái), `ReportsFragment` (biểu đồ), `DashboardFragment`.

### 3.1 Sơ đồ luồng tổng quan (text)

```
SplashActivity
   ├─ isLoggedIn? ──► no ─► LoginActivity ─► RegisterActivity (optional)
   │                                 └─► MainActivity (USER)
   └─ yes ─► role == ADMIN ? ──► AdminActivity (ADMIN)
                                └─► MainActivity (USER)

MainActivity (USER)
   ├─ HomeFragment / AllProductsFragment ─► ProductDetailFragment ─► Add to Cart
   ├─ CartFragment ─► CheckoutActivity
   │      └─ Đặt hàng: OrderService.createOrderWithItemsAndReduceStock
   │            - Tạo Order + OrderItems trong 1 transaction
   │            - Trừ kho Product.quantity theo từng OrderItem
   └─ OrderHistoryActivity ─► OrderDetailActivity
           └─ Huỷ đơn (điều kiện): OrderService.cancelOrderAndRestoreStock
               - Cập nhật trạng thái CANCELLED
               - Cộng trả tồn kho Product.quantity

AdminActivity (ADMIN)
   ├─ ProductsFragment (thêm/sửa/xoá, xem danh sách)
   ├─ OrdersFragment (tìm kiếm/lọc/đổi trạng thái)
   └─ ReportsFragment (doanh thu, top sản phẩm)
```

## 4) Cấu trúc thư mục chính
- `app/src/main/java/com/example/quanlycuahanglaptop/`
  - `app/`: `MainActivity`, `LoginActivity`, `RegisterActivity`, `AdminActivity`.
  - `ui/`: UI chia theo màn hình/ngữ cảnh
    - `fragments/`: `HomeFragment`, `AllProductsFragment`, `CartFragment`, `ProfileFragment`, `ProductDetailFragment`.
    - `checkout/`: `CheckoutActivity`, `PaymentMethodDialog`, `ThankYouActivity`.
    - `order/`: `OrderHistoryActivity`, `OrderDetailActivity`.
    - `admin/`: `DashboardFragment`, `ProductsFragment`, `AddProductFragment`, `EditProductFragment`, `ProductDetailAdminFragment`, `OrdersFragment`, `OrderDetailAdminActivity`, `ReportsFragment`.
    - `components/`: Dialog, decorator, toast tuỳ biến.
    - `adapters/`, `home/`, `cart/`: adapter và item UI.
    - `SplashActivity`, `userinfo/UserInfoActivity`.
  - `service/`: nghiệp vụ (không truy cập UI): `AuthService`, `UserService`, `ProductService`, `OrderService`, `CartService`, `SessionService`.
  - `repository/`: thao tác SQLite: `UserRepository`, `ProductRepository`, `CartItemRepository`, `SessionRepository`.
  - `domain/`: model/enum: `User`, `Product`, `Order`, `OrderItem`, `OrderStatus`, `Role`, `CartItem`.
  - `data/`: `AppDatabase`, `AuthManager` (quản lý kết nối/khởi tạo DB, auth util).
  - `util` & `utils`: `CustomToast`, `TimeUtils`, `SessionManager`, `PasswordHasher`.
- `app/src/main/res/`
  - `layout/`: XML layout cho activity/fragment/item/dialog.
  - `menu/`, `anim/`, `drawable*/`, `values*/`, `xml/`.

## 5) CSDL & Repository (SQLite)
- Tầng `repository` dùng `SQLiteDatabase` từ `AppDatabase` để CRUD.
- Ví dụ:
  - `ProductRepository`: phân trang/sắp xếp/tìm kiếm theo tên.
  - `UserRepository`: tạo người dùng, tìm theo email, xác thực.
  - `SessionRepository`: lưu session đăng nhập.
  - `OrderService` trực tiếp dùng `AppDatabase` cho các truy vấn đơn hàng: tạo đơn, cập nhật trạng thái, lấy item theo `order_id`, thống kê doanh thu, top 3 sản phẩm theo năm, tổng hợp theo trạng thái.

### 5.1 Mô hình dữ liệu & quan hệ bảng

Các bảng chính (SQLite):
- `User(id, name, email UNIQUE, password, phone, role CHECK('ADMIN','USER',...))`
- `Product(id, name, description, price, quantity, image)`
- `"Order"(id, user_id -> User.id, total_price, address, phone, status CHECK('RECEIVED','SHIPPING','DELIVERED','CANCELLED'), created_at)`
- `OrderItem(id, order_id -> Order.id, product_id -> Product.id, quantity, price)`
- `CartItem(id, user_id -> User.id, product_id -> Product.id, quantity, added_at)`
- `Session(id, user_id -> User.id, token, created_at, expires_at)`

Quan hệ:
- 1 `User` ──< nhiều `Order`
- 1 `Order` ──< nhiều `OrderItem`
- 1 `Product` ──< nhiều `OrderItem` và ──< nhiều `CartItem`

Index quan trọng:
- `idx_user_email` trên `User(email)`
- `idx_order_user_id` trên `Order(user_id)`
- `idx_cartitem_user_id` trên `CartItem(user_id)`
- `idx_session_user_id` trên `Session(user_id)`

Lưu ý mapping cột `Order` theo đúng thứ tự schema:
`id(0), user_id(1), total_price(2), address(3), phone(4), status(5), created_at(6)`

### 5.2 Chính sách tồn kho
- Khi đặt hàng: trừ tồn kho theo từng `OrderItem` trong 1 transaction (`createOrderWithItemsAndReduceStock`).
- Khi huỷ đơn (trạng thái `RECEIVED`): cộng trả tồn kho theo `OrderItem` trong 1 transaction (`cancelOrderAndRestoreStock`).
- Nếu không đủ hàng khi đặt: rollback và báo lỗi.

## 6) Service chính (Business logic)
- `AuthService`: đăng ký/đăng nhập, lưu session (`SessionManager`), đăng xuất.
- `UserService`: validate input, băm mật khẩu (SHA-256), CRUD người dùng.
- `ProductService`: validate và gọi `ProductRepository` (phân trang/sort/search, tăng/giảm tồn kho theo batch).
- `CartService`: thêm/xoá/cập nhật số lượng giỏ; tính tổng tiền.
- `OrderService`:
  - Tạo đơn (lưu `created_at` theo VN timezone), cập nhật trạng thái (`OrderStatus`), lấy đơn theo user/trạng thái, lấy chi tiết item; thống kê: doanh thu năm, top 3 sản phẩm/năm, đếm đơn theo trạng thái.
  - Đặt hàng an toàn: `createOrderWithItemsAndReduceStock(order, items)` (transaction).
  - Huỷ đơn an toàn (chỉ khi `RECEIVED`): `cancelOrderAndRestoreStock(orderId)` (transaction).

### 6.1 API nội bộ hay dùng (mô tả nhanh)
- `OrderService.createOrderWithItemsAndReduceStock(order, items): long`
  - Trả về `orderId > 0` nếu thành công; `-1` nếu thiếu hàng/lỗi.
- `OrderService.cancelOrderAndRestoreStock(orderId): boolean`
  - `true` nếu huỷ + hoàn kho thành công; `false` nếu không thoả điều kiện hoặc lỗi.
- `ProductService.decreaseStockBatch(items): boolean` / `increaseStockBatch(items): boolean`

### 6.2 Định dạng thời gian `created_at`
- Lưu ở DB dạng `yyyy-MM-dd HH:mm:ss` (giờ VN).
- Hiển thị dùng `TimeUtils.formatDatabaseTimeToVietnam(created_at)`.

## 7) Điều hướng & UI
- `AndroidManifest.xml`: khai báo launcher `SplashActivity` và các Activity khác (`MainActivity`, `AdminActivity`, `CheckoutActivity`, ...).
- `activity_main.xml`: chứa `FragmentContainerView` + `BottomNavigationView`.
- `AdminActivity`: thay `Fragment` theo `menu_admin` (Dashboard, Products, Orders, Reports).

## 8) Cách build & chạy
- Yêu cầu: Android Studio Flamingo+ (Gradle Wrapper kèm repo), JDK 17.
- Bước chạy:
  1. Mở project bằng Android Studio.
  2. Đồng bộ Gradle (tự động).
  3. Chạy app trên emulator/thiết bị.
- Đăng nhập:
  - Nếu CSDL trống, đăng ký tài khoản USER từ `RegisterActivity`.
  - Tài khoản ADMIN phụ thuộc dữ liệu khởi tạo (tuỳ bản build). Có thể tạo user rồi gán `Role.ADMIN` qua code/debug DB.

### 8.1 Hướng dẫn sử dụng nhanh
- USER:
  - Vào tab Sản phẩm → chọn sản phẩm → Thêm giỏ → Vào Giỏ → Đặt hàng.
  - Xem Lịch sử đơn → chạm một đơn để xem chi tiết → có thể huỷ trong 30 phút nếu trạng thái `RECEIVED`.
- ADMIN:
  - Vào Quản lý Sản phẩm: thêm/sửa/xoá.
  - Vào Đơn hàng: lọc theo trạng thái, xem chi tiết, đổi trạng thái.
  - Vào Báo cáo: xem biểu đồ doanh thu, top sản phẩm.

## 9) Tài nguyên bên thứ ba
- MPAndroidChart (biểu đồ `PieChart`, `BarChart`) dùng trong `ReportsFragment`.

## 10) Mẹo phát triển/Debug
- Dữ liệu lưu cục bộ SQLite: kiểm tra qua `Device File Explorer` → `databases`.
- Nếu lỗi điều hướng role, kiểm tra `SessionManager.getUserRole()` và `AuthService.isLoggedIn()`.
- Nếu danh sách rỗng: xem lại seed data hoặc tạo mới qua UI Admin.

## 11) Lưu ý phân quyền & session
- `SplashActivity` điều hướng theo `SessionManager`:
  - `ADMIN` → `AdminActivity`.
  - Khác → `MainActivity`.
- Đăng xuất qua `AuthService.logout()` xoá session và quay về login.

## 12) Đóng góp
PR/issue: mô tả rõ màn hình, bước tái hiện, logcat (nếu có). Code style Java chuẩn, tên biến có nghĩa, tránh hard-code; chia lớp theo `Controller (Activity/Fragment)` → `Service` → `Repository`.
