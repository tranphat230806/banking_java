# 💳 Banking System

> A modern, secure banking application built with Spring Boot. Perfect for learning enterprise Java development.

[![Java](https://img.shields.io/badge/Java-11+-orange?logo=java)](https://www.java.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.x-green?logo=spring)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.6+-blue?logo=maven)](https://maven.apache.org)
[![License](https://img.shields.io/badge/License-MIT-blue)](LICENSE)

## 📋 Tính Năng Chính

- **🔐 Xác thực an toàn** - Đăng ký, đăng nhập, khôi phục mật khẩu
- **💳 Quản lý tài khoản** - Tạo và quản lý nhiều tài khoản ngân hàng
- **💰 Xử lý giao dịch** -chuyển tiền giữa các tài khoản
- **📝 Quản lý hóa đơn** - Theo dõi và thanh toán hóa đơn
- **📊 Lịch sử giao dịch** - Xem chi tiết tất cả giao dịch
- **🛡️ Bảo mật cao** - Mã hóa bcrypt, JWT token, RBAC

## ⚙️ Stack Công Nghệ

| Lớp          | Công Nghệ              | Phiên Bản |
|--------------|------------------------|-----------|
| **Backend**  | Spring Boot            | 2.x+      |
| **Ngôn Ngữ** | Java                   | 11+       |
| **Database** | MySQL/PostgreSQL + JPA | Latest    |
| **Security** | Spring Security, JWT   | 5.x+      |
| **Build**    | Maven                  | 3.6+      |
| **Test**     | JUnit 5                | 5.x+      |

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

## 🗂️ Cấu Trúc Dự Án

```
banking/
├── src/main/java/com/example/banking/
│   ├── Controller/        # REST endpoints
│   ├── Service/           # Business logic
│   ├── Repository/        # Data access
│   ├── Entity/            # JPA entities
│   ├── DTO/               # Data transfer objects
│   ├── Security/          # Security config
│   └── config/            # Application config
├── src/main/resources/
│   ├── application.properties
│   └── templates/         # HTML templates
├── pom.xml
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
mvn test -Dtest=StudentTestApplicationTests

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

- 📧 Email: support@banking-system.com
- 💬 GitHub Issues: [Report a bug](https://github.com/yourname/banking/issues)
- 📚 Wiki: [Project Wiki](https://github.com/yourname/banking/wiki)

---

<div align="center">

### Made with ❤️ by Banking System Team

[![GitHub Stars](https://img.shields.io/github/stars/yourname/banking?style=social)](https://github.com/yourname/banking)
[![GitHub Forks](https://img.shields.io/github/forks/yourname/banking?style=social)](https://github.com/yourname/banking)
[![GitHub Issues](https://img.shields.io/github/issues/yourname/banking)](https://github.com/yourname/banking/issues)

</div>
