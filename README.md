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

## 6) Service chính (Business logic)
- `AuthService`: đăng ký/đăng nhập, lưu session (`SessionManager`), đăng xuất.
- `UserService`: validate input, băm mật khẩu (SHA-256), CRUD người dùng.
- `ProductService`: validate và gọi `ProductRepository` (phân trang/sort/search).
- `CartService`: thêm/xoá/cập nhật số lượng giỏ; tính tổng tiền.
- `OrderService`:
  - Tạo đơn (lưu `created_at` theo VN timezone), cập nhật trạng thái (`OrderStatus`), lấy đơn theo user/trạng thái, lấy chi tiết item; thống kê: doanh thu năm, top 3 sản phẩm/năm, đếm đơn theo trạng thái.

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
