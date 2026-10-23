# 🛒 ShopEase Marketplace — Java Servlet E-Commerce Platform

ShopEase là nền tảng thương mại điện tử đa người dùng (Multi-Vendor Marketplace) được xây dựng trên nền tảng **Java Servlet/Jakarta EE**, **Hibernate / JPA**, kết hợp giao diện Web tĩnh tối ưu tốc độ và cơ sở dữ liệu **Microsoft SQL Server**.

Hệ thống hỗ trợ quản lý danh mục, sản phẩm, giỏ hàng thông minh, đặt hàng, quản trị viên (Admin), người bán (Seller) và người mua (Customer), cùng cơ chế xác thực bảo mật chuẩn **JWT lưu hoàn toàn trong HttpOnly Cookie**.

---

## ✨ Điểm nổi bật & Tính năng chính

- **Bảo mật xác thực HttpOnly Cookie**:
  - `access_token` và `refresh_token` được lưu độc quyền trong Cookie an toàn (`HttpOnly`, `SameSite=Lax`), ngăn chặn hoàn toàn tấn công XSS trộm token qua JavaScript.
  - Tự động gia hạn phiên đăng nhập (Silent Token Auto-refresh) trong `JwtFilter` khi access token hết hạn nhưng refresh token còn hợp lệ.
  - Phân quyền nghiêm ngặt với RoleGuard: `CUSTOMER`, `SELLER`, `ADMIN`.
- **Quản lý Giỏ hàng thông minh**:
  - Khách vãng lai (Guest): Lưu trữ giỏ hàng tạm thời trên Client.
  - Sau khi đăng nhập: Tự động đồng bộ và gộp sản phẩm từ giỏ tạm vào cơ sở dữ liệu.
- **Phân hệ Người bán (Seller)**: Quản lý đăng bán, cập nhật, kiểm soát số lượng tồn và đơn hàng của cửa hàng.
- **Phân hệ Quản trị (Admin)**: Quản lý người dùng (khóa, mở, phân quyền), thống kê doanh thu, quản trị danh mục sản phẩm toàn sàn.
- **Containerization**: Khởi chạy toàn bộ hệ thống (App + Database) chỉ với một lệnh Docker duy nhất.

---

## 🛠️ Công nghệ sử dụng

| Tầng | Công nghệ |
|------|-----------|
| **Backend** | Java Servlets (Jakarta EE 10), JPA 3.1 / Hibernate ORM |
| **Frontend** | HTML5, CSS3, JavaScript (Fetch API với `credentials: 'include'`) |
| **Cơ sở dữ liệu** | Microsoft SQL Server 2022 |
| **Xác thực** | JWT (auth0-jwt) + HttpOnly Cookies (Access Token & Refresh Token) |
| **Build & Runtime** | Apache Maven, Apache Tomcat 10, Docker & Docker Compose |

---

## 🚀 Hướng dẫn cài đặt & Khởi chạy

### Cách 1: Chạy bằng Docker Compose (Khuyên dùng)

Yêu cầu máy đã cài đặt **Docker** và **Docker Compose**.

1. Mở terminal tại thư mục gốc của dự án:
   ```bash
   docker compose up -d --build
   ```

2. Docker sẽ tự động:
   - Khởi động SQL Server và cấu hình cơ sở dữ liệu `ecommerce_db`.
   - Build ứng dụng qua Maven và deploy lên Tomcat 10.
   - Nạp dữ liệu mẫu ban đầu (`seed-data.sql`).

3. Truy cập website tại: **`http://localhost:8080/index.html`**

**Lệnh quản lý container:**
```bash
# Xem logs ứng dụng
docker compose logs -f app

# Xem logs cơ sở dữ liệu
docker compose logs -f sqlserver

# Dừng hệ thống (giữ lại dữ liệu DB)
docker compose down

# Dừng và xóa toàn bộ dữ liệu DB
docker compose down -v
```

---

### Cách 2: Chạy trực tiếp từ mã nguồn Local

1. **Cài đặt Database**: Cài đặt SQL Server và tạo cơ sở dữ liệu `ecommerce_db`.
2. **Cấu hình biến môi trường**: Thiết lập file `.env` hoặc truyền biến môi trường:
   - `DB_HOST=localhost`
   - `DB_PORT=1433`
   - `DB_NAME=ecommerce_db`
   - `DB_USER=sa`
   - `DB_PASSWORD=YourPassword123!`
   - `JWT_SECRET=your-secure-jwt-secret-key`
3. **Build mã nguồn**:
   ```bash
   mvn clean package
   ```
4. Copy file `.war` sinh ra trong thư mục `target/` vào thư mục `webapps/ROOT.war` của máy chủ Tomcat 10 và khởi động Tomcat.

---

## 👥 Tài khoản thử nghiệm (Seed Data)

Hệ thống có sẵn các tài khoản mẫu để kiểm thử các phân quyền:

| Phân quyền | Email | Mật khẩu | Mô tả quyền hạn |
|------------|-------|----------|-----------------|
| **ADMIN** | `admin@gmail.com` | `admin123` | Quản trị toàn hệ thống, người dùng, báo cáo |
| **SELLER** | `nora.seller@gmail.com` | `seller123` | Đăng sản phẩm, quản lý kho hàng và đơn hàng |
| **SELLER** | `samir.seller@gmail.com` | `seller123` | Người bán hàng |
| **CUSTOMER** | `amina.customer@gmail.com` | `customer123` | Mua hàng, xem giỏ hàng, đặt hàng, quản lý profile |
| **CUSTOMER** | `youssef.customer@gmail.com` | `customer123` | Khách mua hàng |

---

## 🌐 Các trang giao diện (Web App URLs)

| Trang | URL |
|-------|-----|
| Trang chủ | `http://localhost:8080/index.html` |
| Đăng nhập | `http://localhost:8080/login.html` |
| Đăng ký | `http://localhost:8080/signup.html` |
| Cửa hàng / Sản phẩm | `http://localhost:8080/store.html` |
| Giỏ hàng | `http://localhost:8080/cart.html` |
| Đơn hàng của tôi | `http://localhost:8080/Orders.html` |
| Thông tin cá nhân | `http://localhost:8080/profile.html` |
| Quản lý cho Người bán | `http://localhost:8080/products.html` |
| Trang quản trị Admin | `http://localhost:8080/admin.html` |

---

## 📡 Danh sách API chính (`/api/*`)

### 1. Xác thực (Auth)
- `POST /api/auth/signup`: Đăng ký tài khoản (tự động cấp cookie xác thực).
- `POST /api/auth/login`: Đăng nhập (gắn `access_token` & `refresh_token` vào HttpOnly Cookie).
- `POST /api/auth/refresh`: Gia hạn `access_token` từ `refresh_token` trong cookie.
- `POST /api/auth/logout`: Đăng xuất (xóa toàn bộ cookie trên trình duyệt).

### 2. Sản phẩm (Products)
- `GET /api/products`: Danh sách sản phẩm (Công khai).
- `GET /api/products/{id}`: Chi tiết sản phẩm (Công khai).
- `POST /api/products`: Thêm sản phẩm mới (SELLER, ADMIN).
- `PUT /api/products/{id}`: Sửa sản phẩm (Người bán sở hữu, ADMIN).
- `DELETE /api/products/{id}`: Xóa sản phẩm (Người bán sở hữu, ADMIN).

### 3. Danh mục (Categories)
- `GET /api/categories`: Lấy danh sách danh mục.
- `POST /api/categories`: Thêm danh mục (ADMIN).
- `PUT /api/categories/{id}`: Sửa danh mục (ADMIN).
- `DELETE /api/categories/{id}`: Xóa danh mục (ADMIN).

### 4. Giỏ hàng & Đơn hàng (Cart & Orders)
- `GET /api/cart`: Lấy giỏ hàng người dùng hiện tại.
- `POST /api/cart`: Thêm/sửa số lượng sản phẩm trong giỏ.
- `POST /api/orders`: Tạo đơn hàng mới từ giỏ hàng.
- `GET /api/orders/customer`: Lịch sử đơn hàng của người dùng.
- `GET /api/orders/{id}`: Xem chi tiết đơn hàng.

### 5. Quản trị hệ thống (Admin)
- `GET /api/admin/stats`: Thống kê tổng quan hệ thống.
- `GET /api/admin/users`: Danh sách tất cả người dùng.
- `PUT /api/admin/users/{id}/role`: Thay đổi quyền người dùng.
- `PUT /api/admin/users/{id}/status`: Khóa hoặc kích hoạt tài khoản.

---

## 📂 Cấu trúc thư mục dự án

```text
ShopEase-Marketplace/
├── src/main/
│   ├── java/com/ecommerce/
│   │   ├── entity/          # Các thực thể JPA (User, Product, Order, Cart,...)
│   │   ├── repository/      # Tầng truy xuất dữ liệu (Data Access Layer)
│   │   ├── service/         # Nghiệp vụ logic hệ thống
│   │   ├── servlet/         # Các Servlet REST API
│   │   ├── filter/          # JwtFilter (xác thực HttpOnly Cookie), RoleGuard
│   │   └── util/            # JwtUtil, CookieUtil, PasswordUtil, JPAUtil
│   ├── resources/
│   │   ├── META-INF/persistence.xml
│   │   └── seed-data.sql    # Dữ liệu ban đầu
│   └── webapp/              # Giao diện Frontend (HTML, CSS, JavaScript)
├── Dockerfile               # File đóng gói Tomcat & App
├── docker-compose.yml       # Điều phối Docker Compose (App + MSSQL)
├── pom.xml                  # Quản lý thư viện Maven
└── Readme.md                # Tài liệu dự án
```
