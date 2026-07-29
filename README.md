# 💳 Banking System

> A modern, secure banking application built with Spring Boot. Perfect for learning enterprise Java development.

[![Java](https://img.shields.io/badge/Java-11+-orange?logo=java)](https://www.java.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.x-green?logo=spring)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.6+-blue?logo=maven)](https://maven.apache.org)
[![License](https://img.shields.io/badge/License-MIT-blue)](LICENSE)

## 📋 Tính Năng Chính

- **🔐 Xác thực an toàn** - Đăng ký, đăng nhập, khôi phục mật khẩu
- **💳 Quản lý tài khoản** - Tạo và quản lý nhiều tài khoản ngân hàng
- **💰 Xử lý giao dịch** - chuyển tiền giữa các tài khoản
- **🤖 Nhận Diện Khuôn Mặt (FaceID)** - Tích hợp AI OpenCV để xác thực sinh trắc học bảo vệ các giao dịch có giá trị lớn
- **📝 Quản lý hóa đơn** - Theo dõi và thanh toán hóa đơn
- **📊 Lịch sử giao dịch** - Xem chi tiết tất cả giao dịch
- **👨‍💼 Quản Trị & Hỗ Trợ Khách Hàng (Admin)** - Portal dành riêng cho Admin giám sát hệ thống, hỗ trợ giải đáp người dùng và xử lý các vấn đề tài khoản khẩn cấp (vd: mở khóa giao dịch)
- **🛡️ Bảo mật cao** - Mã hóa bcrypt, JWT token, RBAC, PIN code

## ⚙️ Stack Công Nghệ

| Lớp / Chức Năng          | Công Nghệ                                  | Phiên Bản |
|--------------------------|--------------------------------------------|-----------|
| **Ngôn Ngữ**             | Java                                       | 21        |
| **Backend Framework**    | Spring Boot                                | 3.x+      |
| **Giao Diện (Frontend)** | Thymeleaf, HTML5, CSS3, Vanilla JS         | -         |
| **Cơ Sở Dữ Liệu**        | MySQL & Spring Data JPA                    | Latest    |
| **Bảo Mật**              | Spring Security, JWT, Bcrypt               | -         |
| **Real-time (Chat)**     | Spring WebSocket, STOMP, SockJS            | -         |
| **Nhận Diện Khuôn Mặt**  | OpenCV (Java)                              | 4.7.0     |
| **Mã QR (Tạo/Quét)**     | ZXing (Zebra Crossing)                     | 3.5.3     |
| **Biến Môi Trường**      | dotenv-java                                | 3.0.0     |
| **Build Tool**           | Maven                                      | 3.6+      |

## 🚀 Quick Start

### 1. Clone & Cài Đặt

```bash
git clone <repository-url>
cd banking
mvn clean install
```

### 2. Cấu Hình Database

Tạo database:

```sql
CREATE
DATABASE banking_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Cập nhật `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/banking_db
spring.datasource.username=root
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
server.port=8080
```

### 3. Chạy Ứng Dụng

```bash
mvn spring-boot:run
```

Truy cập: **http://localhost:8080**

## 📚 API Endpoints

### 🔐 Authentication

```bash
# Đăng ký
POST /api/auth/register
{"email":"user@example.com","password":"Pass@123","fullName":"John Doe"}

# Đăng nhập
POST /api/auth/login
{"email":"user@example.com","password":"Pass@123"}

# Khôi phục mật khẩu
POST /api/auth/forgot-password
{"email":"user@example.com"}
```

### 💳 Accounts

```bash
# Lấy danh sách tài khoản
GET /api/accounts
Authorization: Bearer <token>

# Tạo tài khoản mới
POST /api/accounts
{"accountType":"SAVINGS","initialBalance":1000000}

# Xem chi tiết tài khoản
GET /api/accounts/{accountId}
Authorization: Bearer <token>
```

### 💰 Transactions

```bash
# Xem lịch sử giao dịch
GET /api/transactions?accountId=1&limit=10

# Tạo giao dịch
POST /api/transactions
{"type":"TRANSFER","fromAccountId":1,"toAccountId":2,"amount":500000}
```

### 📝 Bills

```bash
# Xem hóa đơn
GET /api/bills?status=PENDING

# Thanh toán hóa đơn
PUT /api/bills/{billId}/pay
{"accountId":1,"paymentMethod":"ACCOUNT_TRANSFER"}
```

## 💡 Chi Tiết Luồng Hoạt Động (Core Logic)

### 1. Luồng Chuyển Tiền & Bảo Mật 2 Lớp (PIN + FaceID)
Hệ thống phân chia rủi ro giao dịch dựa trên mức tiền:
- **Dưới 10.000.000 VNĐ:** Người dùng chỉ cần xác thực bằng mã **PIN**. Nếu nhập sai mã PIN quá 5 lần, tính năng chuyển khoản sẽ bị khóa tạm thời và hệ thống sẽ tự động gửi mã OTP qua Email để hỗ trợ mở khóa.
- **Từ 10.000.000 VNĐ trở lên:** Sau bước nhập PIN thành công, hệ thống bắt buộc điều hướng (`redirect`) sang màn hình **FaceID**. Hệ thống sẽ dùng camera để chụp ảnh và đẩy về Server. Model AI của thư viện **OpenCV** sẽ nhận diện và đối chiếu khuôn mặt. Giao dịch chỉ thành công khi khuôn mặt khớp với dữ liệu lúc đăng ký.

### 2. Quét Mã QR Tự Động Bằng ZXing
Trải nghiệm chuyển khoản được tối ưu tối đa, chống thao tác nhầm lẫn:
- **Tạo mã (Bên nhận):** Hệ thống dùng thư viện **ZXing** (`QRCodeWriter`) để mã hóa số tài khoản (`accountCode`) thành một ảnh QR định dạng PNG và trả thẳng về trình duyệt hiển thị.
- **Quét mã (Bên chuyển):** Khi người dùng dùng camera quét, trình duyệt bắt ảnh và gửi lên API. Server giải mã bức ảnh để lấy ra số tài khoản, sau đó Frontend tự động điền vào form và tự động truy xuất hiển thị **Tên người nhận** ngay trên màn hình.

### 3. Hệ Thống Chat Hỗ Trợ Real-time (WebSocket)
Hỗ trợ tương tác mượt mà giữa User và Admin dựa trên **Spring WebSocket + STOMP**:
- **Auto-reply Bot (Trợ lý ảo):** Tin nhắn gửi lên sẽ được quét từ khóa. Nếu người dùng nhắn các từ như *"xin chào", "quên mật khẩu", "chuyển tiền"*, Bot sẽ trả lời ngay lập tức theo kịch bản có sẵn.
- **Live Support:** Nếu rớt khỏi kịch bản của Bot, tin nhắn sẽ được đẩy trực tiếp lên màn hình Admin (`/topic/admin`). Admin có thể chat trực tiếp 1:1 với user hoặc phát thanh (`Broadcast`) thông báo khẩn cấp tới tất cả những ai đang online.
- Dữ liệu chat được tổ chức lưu trữ trên In-memory (`ConcurrentHashMap`) giúp tốc độ phản hồi tính bằng mili-giây.

## 🗂️ Cấu Trúc Dự Án

```text
banking_java/
├── src/
│   ├── main/
│   │   ├── java/com/example/banking/
│   │   │   ├── config/            # Cấu hình ứng dụng (WebMvc, Security...)
│   │   │   ├── Controller/        # Xử lý các request (REST endpoints & Web)
│   │   │   ├── DTO/               # Data transfer objects
│   │   │   ├── Entity/            # JPA entities (Models)
│   │   │   ├── Repository/        # Tương tác với Database
│   │   │   ├── Security/          # Cấu hình bảo mật (Jwt, Filters...)
│   │   │   ├── Service/           # Xử lý logic nghiệp vụ
│   │   │   └── BankingApplication.java # File chạy Spring Boot chính
│   │   └── resources/
│   │       ├── static/            # Các file tĩnh (CSS, JS, Images...)
│   │       ├── templates/         # Giao diện HTML (Thymeleaf)
│   │       ├── application.properties # File cấu hình Spring Boot
│   │       ├── haarcascade_frontalface_default.xml # Model cho nhận diện khuôn mặt OpenCV
│   │       └── insert_admin.sql   # Script SQL tạo sẵn admin
│   └── test/                      # Mã nguồn test
├── uploads/             # Thư mục chứa file người dùng upload (thường bị bỏ qua bởi git)
├── .env                 # Chứa các biến môi trường cấu hình dự án
├── .gitignore           # File cấu hình bỏ qua của Git
├── pom.xml              # Khai báo thư viện Maven
└── README.md
```

## 🛠️ Cài Đặt & Chạy

### Yêu Cầu

- Java 11+
- Maven 3.6+
- MySQL 5.7+ hoặc PostgreSQL 10+

### Các Bước

**1. Clone Repository**

```bash
git clone <repository-url>
cd banking
```

**2. Tạo Database**

```sql
CREATE
DATABASE banking_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

**3. Cấu Hình Database**
Sửa `src/main/resources/application.properties` với thông tin database của bạn.

**4. Cài Dependencies & Chạy**

```bash
mvn clean install
mvn spring-boot:run
```

**5. Kiểm Tra**

```bash
curl http://localhost:8080/actuator/health
```

## 📖 Core Components

### Entities

- **UserClass** - User information and credentials
- **AccountClass** - Bank accounts
- **TransactionClass** - Transaction records
- **BillClass** - Bill information
- **EventClass** - System events

### Services

- **UsersService** - User management
- **AccountService** - Account operations
- **TransactionService** - Transaction processing
- **BillService** - Bill management

### DTOs

- **LoginDTO** - Login credentials
- **RegisterDTO** - Registration data
- **TransactionDTO** - Transaction data
- **BillDTO** - Bill information

## 🧪 Testing

```bash
# Chạy tất cả test
mvn test

# Chạy test cụ thể
mvn test -Dtest=BankingApplicationTests

# Test coverage
mvn test jacoco:report
```

## ⚡ Troubleshooting

### Lỗi "Connection refused"

```bash
# Kiểm tra MySQL đã chạy?
mysql -u root -p

# Cập nhật thông tin database trong application.properties
```

### Port 8080 đang được dùng

```properties
# Đổi port trong application.properties
server.port=8081
```

### Database encoding lỗi

```sql
DROP
DATABASE banking_db;
CREATE
DATABASE banking_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### API trả về 401 Unauthorized

```bash
# Kiểm tra token JWT hợp lệ
curl -H "Authorization: Bearer <your_token>" http://localhost:8080/api/accounts
```

## 🤝 Đóng Góp

1. Fork Repository
2. Tạo branch: `git checkout -b feature/amazing-feature`
3. Commit: `git commit -m "feat: add amazing feature"`
4. Push: `git push origin feature/amazing-feature`
5. Tạo Pull Request

**Commit Message Format:**

- `feat: new feature`
- `fix: bug fix`
- `docs: documentation`
- `test: add tests`
- `refactor: code refactoring`

## 📋 Roadmap

### Phase 1: MVP ✅

- [x] User authentication (Login/Register)
- [x] Account management
- [x] Transaction processing
- [x] Bill management
- [x] Password reset

### Phase 2: Enhancements (Coming Soon)

- Mobile App
- Two-factor authentication
- Budget planning
- Advanced analytics
- Real-time notifications

## 🔒 Security

- ✅ **Xác Thực Khuôn Mặt (OpenCV FaceID):** Tự động yêu cầu xác thực bằng khuôn mặt khi thực hiện các giao dịch chuyển tiền vượt hạn mức an toàn (VD: > 10.000.000đ).
- ✅ OWASP Top 10 compliant
- ✅ SQL Injection prevention
- ✅ XSS protection
- ✅ CSRF tokens
- ✅ Bcrypt password hashing
- ✅ JWT token validation
- ✅ Role-based access control

### Report Security Issues

Tìm thấy lỗ hổng bảo mật? Gửi email tới: **security@banking-system.com**

## 📄 License

MIT License - Xem [LICENSE](LICENSE) để biết chi tiết.

## 📚 Tài Liệu Tham Khảo

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security Guide](https://spring.io/guides/gs/securing-web/)
- [JPA/Hibernate Tutorial](https://www.baeldung.com/jpa-hibernate-cascading)
- [RESTful API Best Practices](https://restfulapi.net/)
- [Java Design Patterns](https://refactoring.guru/design-patterns/java)

## 📞 Contact

- 📧 Email: phat.tran09192005@gmail.com
- 💬 GitHub Issues: [Report a bug](https://github.com/yourname/banking/issues)
- 📚 Wiki: [Project Wiki](https://github.com/yourname/banking/wiki)

---

<div align="center">

### Made with ❤️ by Banking System Team

[![GitHub Stars](https://img.shields.io/github/stars/yourname/banking?style=social)](https://github.com/yourname/banking)
[![GitHub Forks](https://img.shields.io/github/forks/yourname/banking?style=social)](https://github.com/yourname/banking)
[![GitHub Issues](https://img.shields.io/github/issues/yourname/banking)](https://github.com/yourname/banking/issues)

</div>
