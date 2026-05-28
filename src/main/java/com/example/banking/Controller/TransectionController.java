package com.example.banking.Controller;

import com.example.banking.DTO.BillDTO;
import com.example.banking.DTO.RegisterDTO;
import com.example.banking.DTO.TransectionDTO;
import com.example.banking.Entity.AccountClass;
import com.example.banking.Entity.BillClass;
import com.example.banking.Entity.TransactionsClass;
import com.example.banking.Entity.UserClass;
import com.example.banking.Repository.*;
import com.example.banking.Security.CustomUserDetails;
import com.example.banking.Service.*;
import jakarta.servlet.http.HttpSession;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class TransectionController {
    @Autowired
    AccountService ser;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private BillRepository billrepo;
    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userrepo;

    @Autowired
    EventRepository eventrepo;

    @Autowired
    private FaceIdService faceIdService;

    @Autowired
    private TransferSecurityService transferSecurityService;

    // Login
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    //Dashboard
    @GetMapping("/dashboard")
    public String home(Model model,
                       @AuthenticationPrincipal CustomUserDetails userDetails) {

        String name = userDetails.getUsername();

        UserClass user = userrepo.findByUsername(name)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        model.addAttribute("user", user);
        //event
        model.addAttribute("event", eventrepo.findByStatusTrueOrderByDisLayOrDerAsc());
        // transaction history
        AccountClass account = user.getAccounts();
        List<TransactionsClass> transactions = transactionRepository.findTTop5ByFromAccountOrderByCreatedDesc(account);
        model.addAttribute("list", transactions != null ? transactions : Collections.emptyList());
        return "dashboard";
    }

    // Nếu viết chung vào TransectionController của bạn, hãy thay thế hàm /admin/dashboard cũ bằng đoạn mã này:
    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model, @AuthenticationPrincipal CustomUserDetails adminDetails) {
        model.addAttribute("adminName", adminDetails.getUsername());

        // 1. Thống kê số lượng User trong hệ thống
        long totalUsers = userrepo.count();
        long activeUsers = userrepo.count();
        long lockedUsers = 0;

        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("activeUsers", activeUsers);
        model.addAttribute("lockedUsers", lockedUsers);

        // 2. Thống kê tổng số tiền giao dịch thực tế trong ngày hôm nay
        java.math.BigDecimal dailyTotal = java.math.BigDecimal.ZERO;
        java.time.LocalDate today = java.time.LocalDate.now();
        List<TransactionsClass> allTransactions = transactionRepository.findAll();
        for (TransactionsClass t : allTransactions) {
            if (t.getAmount() != null && t.getCreated() != null) {
                if (t.getCreated().toLocalDate().isEqual(today)) {
                    dailyTotal = dailyTotal.add(t.getAmount());
                }
            }
        }
        model.addAttribute("dailyTotal", dailyTotal);

        // 3. Chuẩn bị mảng dữ liệu doanh thu 12 tháng phát sinh thực tế trong năm nay để vẽ biểu đồ Chart.js
        List<Long> monthlyChartData = new java.util.ArrayList<>(java.util.Collections.nCopies(12, 0L));
        int currentYear = java.time.LocalDate.now().getYear();
        for (TransactionsClass t : allTransactions) {
            if (t.getAmount() != null && t.getCreated() != null) {
                if (t.getCreated().getYear() == currentYear) {
                    int monthIdx = t.getCreated().getMonthValue() - 1; // 0-11
                    if (monthIdx >= 0 && monthIdx < 12) {
                        monthlyChartData.set(monthIdx, monthlyChartData.get(monthIdx) + t.getAmount().longValue());
                    }
                }
            }
        }
        model.addAttribute("monthlyChartData", monthlyChartData);

        // 4. Lấy danh sách toàn bộ Người dùng trong hệ thống để thực hiện quản lý và gỡ khóa
        List<UserClass> userList = userrepo.findAll();
        model.addAttribute("userList", userList);

        return "dashboard_admin"; // Khớp với tên file template của bạn
    }

    // API xử lý gỡ Lock cho các user bị khóa chức năng chuyển tiền (Tính năng Transfer Lock sẵn có trong code của bạn)
    @PostMapping("/admin/unlock-user-transfer")
    @ResponseBody
    public Map<String, Object> adminUnlockUserTransfer(@RequestParam Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            UserClass user = userrepo.findById(userId).orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

            // Gọi service xử lý mở khóa hoặc reset trực tiếp trạng thái lock trong trường hợp khẩn cấp
            // Ở đây ta dùng trực tiếp phương thức reset cơ chế bảo vệ của bạn
            user.setTransferLocked(false);
            userrepo.save(user);

            response.put("success", true);
            response.put("message", "Gỡ khóa giao dịch thành công cho người dùng " + user.getFullName());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    //Transection
    @GetMapping("/banking")
    public String showForm(Model model,
                           @AuthenticationPrincipal CustomUserDetails user,
                           @RequestParam(required = false, name = "account") String account,
                           @RequestParam(required = false) String amount,
                           @RequestParam(required = false) String description) {

        model.addAttribute("fullname", user.getFullName());

        // ưu tiên dữ liệu QR
        if (account != null) {

            String[] data = account.split("\\|");

            // account
            if (data.length > 0) {
                model.addAttribute("toAccount", data[0]);
            }

            // amount từ QR
            if (data.length > 1 && !data[1].isBlank()) {
                model.addAttribute("amount", data[1]);
            }

            // description từ QR
            if (data.length > 2 && !data[2].isBlank()) {
                model.addAttribute("description", data[2]);
            }
        }

        // nếu không có QR thì dùng param thường
        if (amount != null && !amount.isBlank()) {
            model.addAttribute("amount", amount);
        }

        if (description != null && !description.isBlank()) {
            model.addAttribute("description", description);
        }

        return "fromCK";
    }

    @PostMapping("/banking")
    public String Transetion_in_banking(@ModelAttribute TransectionDTO request,
                                        @AuthenticationPrincipal CustomUserDetails userDetails,
                                        BillDTO billdto, Model model, HttpSession session) {
        try {
            UserClass user = userrepo.findByUsername(userDetails.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));

            // Check if PIN is set up
            if (!transferSecurityService.isPinSetup(user)) {
                model.addAttribute("message", "Bạn chưa thiết lập mã PIN chuyển tiền. Vui lòng thiết lập trong Profile.");
                return "fromCK";
            }

            // Save pending transaction to session and redirect to PIN verify
            session.setAttribute("pendingTransfer", request);
            session.setAttribute("pendingBill", billdto);
            return "redirect:/transfer/verify-pin";
        } catch (Exception e) {
            model.addAttribute("message", e.getMessage());
        }
        return "fromCK";
    }

    // --- FaceID Endpoints ---
    @PostMapping("/face/register")
    @ResponseBody
    public Map<String, Object> registerFace(@RequestBody Map<String, String> body, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        try {
            UserClass user = userrepo.findByUsername(userDetails.getUsername()).orElseThrow();
            boolean success = faceIdService.registerFace(user, body.get("image"));
            response.put("success", success);
            if (!success) response.put("message", "Không tìm thấy khuôn mặt, vui lòng thử lại.");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @GetMapping("/face-verify")
    public String faceVerifyPage(HttpSession session, Model model) {
        if (session.getAttribute("pendingTransfer") == null) {
            return "redirect:/banking";
        }
        return "faceVerify";
    }

    @PostMapping("/face-verify/execute")
    @ResponseBody
    public Map<String, Object> executeFaceVerify(@RequestBody Map<String, String> body, @AuthenticationPrincipal CustomUserDetails userDetails, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            UserClass user = userrepo.findByUsername(userDetails.getUsername()).orElseThrow();
            boolean isVerified = faceIdService.verifyFace(user, body.get("image"));

            if (isVerified) {
                TransectionDTO pendingTransfer = (TransectionDTO) session.getAttribute("pendingTransfer");
                BillDTO pendingBill = (BillDTO) session.getAttribute("pendingBill");

                if (pendingTransfer != null) {
                    BillClass bill = ser.transferMoney(pendingTransfer, userDetails, pendingBill);
                    session.removeAttribute("pendingTransfer");
                    session.removeAttribute("pendingBill");
                    response.put("success", true);
                    response.put("redirect", "/bill/" + bill.getId());
                } else {
                    response.put("success", false);
                    response.put("message", "Giao dịch không tồn tại.");
                }
            } else {
                response.put("success", false);
                response.put("message", "Xác thực khuôn mặt thất bại.");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }
    // ------------------------

    @PostMapping("/profile/upload-avatar")
    public String uploadAvatar(@RequestParam("avatar") MultipartFile file,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {

        try {
            String username = principal.getName();

            // 1. validate file rỗng
            if (file == null || file.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Vui lòng chọn ảnh.");
                return "redirect:/profile";
            }

            // 2. check loại file (QUAN TRỌNG)
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                redirectAttributes.addFlashAttribute("error", "Chỉ được upload file ảnh.");
                return "redirect:/profile";
            }

            // 3. tạo tên file an toàn hơn
            String originalName = file.getOriginalFilename();
            String extension = originalName != null && originalName.contains(".")
                    ? originalName.substring(originalName.lastIndexOf("."))
                    : ".jpg";

            String filename = username + "_" + System.currentTimeMillis() + extension;

            // 4. tạo folder nếu chưa có
            Path uploadPath = Paths.get("uploads/avatars");
            Files.createDirectories(uploadPath);

            // 5. lưu file
            Path filePath = uploadPath.resolve(filename);
            file.transferTo(filePath);

            // 6. update DB
            UserClass user = userrepo.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            user.setAvatar("/uploads/avatars/" + filename);
            userrepo.save(user);

            redirectAttributes.addFlashAttribute("success", "Cập nhật avatar thành công!");

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Upload thất bại!");
        }

        return "redirect:/profile";
    }

    // --- Transfer PIN Endpoints ---
    @PostMapping("/profile/setup-pin")
    @ResponseBody
    public Map<String, Object> setupPin(@RequestBody Map<String, String> body, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        try {
            UserClass user = userrepo.findByUsername(userDetails.getUsername()).orElseThrow();
            transferSecurityService.setupPin(user, body.get("pin"));
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @GetMapping("/transfer/verify-pin")
    public String verifyPinPage(HttpSession session, Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (session.getAttribute("pendingTransfer") == null) {
            return "redirect:/banking";
        }
        UserClass user = userrepo.findByUsername(userDetails.getUsername()).orElseThrow();
        model.addAttribute("isLocked", user.isTransferLocked());
        return "pinVerify";
    }

    @PostMapping("/transfer/verify-pin")
    @ResponseBody
    public Map<String, Object> verifyPin(@RequestBody Map<String, String> body, @AuthenticationPrincipal CustomUserDetails userDetails, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            UserClass user = userrepo.findByUsername(userDetails.getUsername()).orElseThrow();
            boolean isValid = transferSecurityService.verifyPin(user, body.get("pin"));

            if (isValid) {
                TransectionDTO pendingTransfer = (TransectionDTO) session.getAttribute("pendingTransfer");
                BillDTO pendingBill = (BillDTO) session.getAttribute("pendingBill");

                if (pendingTransfer != null) {
                    // Check FaceID requirement
                    if (pendingTransfer.getAmount() != null && pendingTransfer.getAmount().compareTo(new java.math.BigDecimal("9999999")) > 0) {
                        if (!user.isFaceRegistered()) {
                            response.put("success", false);
                            response.put("message", "Giao dịch yêu cầu FaceID nhưng chưa đăng ký.");
                            return response;
                        }
                        response.put("success", true);
                        response.put("redirect", "/face-verify");
                    } else {
                        BillClass bill = ser.transferMoney(pendingTransfer, userDetails, pendingBill);
                        session.removeAttribute("pendingTransfer");
                        session.removeAttribute("pendingBill");
                        response.put("success", true);
                        response.put("redirect", "/bill/" + bill.getId());
                    }
                } else {
                    response.put("success", false);
                    response.put("message", "Giao dịch không tồn tại.");
                }
            } else {
                response.put("success", false);
                response.put("message", user.isTransferLocked() ? "LOCKED" : "Mã PIN sai.");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @PostMapping("/transfer/unlock")
    @ResponseBody
    public Map<String, Object> unlockTransfer(@RequestBody Map<String, String> body, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        try {
            UserClass user = userrepo.findByUsername(userDetails.getUsername()).orElseThrow();
            boolean success = transferSecurityService.unlockAccount(user, body.get("otp"));
            response.put("success", success);
            if (!success) response.put("message", "OTP không hợp lệ hoặc đã hết hạn.");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }
    // ------------------------------

    //profileUser
    @GetMapping("/profile")
    public String fromProfile(Model model, @AuthenticationPrincipal CustomUserDetails user) {
        String username = user.getUsername();
        UserClass userclass = userrepo.findByUsername(username).orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        AccountClass accountClass = accountRepository.findByUserId(userclass.getId()).orElseThrow(() -> new RuntimeException("không tìm thấy account"));
        model.addAttribute("user", userclass);
        model.addAttribute("account", accountClass);
        return "profileUser";
    }

    //History Transection
    @GetMapping("/history")
    public String fromHistory(Model model, @AuthenticationPrincipal CustomUserDetails user) {
        UserClass userClass = userrepo.findByUsername(user.getUsername()).orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
        AccountClass account = accountRepository.findByUserId(userClass.getId()).orElseThrow(() -> new RuntimeException("không tìm thấy tài khoản này!!!"));
        model.addAttribute("list", transactionRepository.findByFromAccountOrToAccountOrderByCreatedDesc(account, account));
        model.addAttribute("account", account);
        model.addAttribute("user", userClass);
        return "historyTransection";
    }

    @Autowired
    RegisterService createser;

    //Create
    @GetMapping("/create")
    public String fromCreate() {
        return "formCreate";
    }

    @PostMapping(value = "/create", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public String Register_in_bank(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam(required = false) MultipartFile avatar,
            Model model) {
        try {
            RegisterDTO dto = new RegisterDTO();
            dto.setUsername(username);
            dto.setPassword(password);
            dto.setFullName(fullName);
            dto.setEmail(email);
            dto.setPhone(phone);
            dto.setAvatar(avatar);

            // Kiểm tra xem username đã tồn tại chưa
            if (createser.isUsernameTaken(dto.getUsername())) {
                model.addAttribute("error", "Code đã tồn tại!!!!");
                return "formCreate";  // Trả về trang tạo tài khoản nếu có lỗi
            }
            // Gọi service để tạo user và tài khoản
            createser.registerUser(dto);
            model.addAttribute("success", "Bạn đã đăng kí thành công!!!");
            return "redirect:/login";  // Redirect đến trang login sau khi đăng ký thành công
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Đã có lỗi xảy ra, vui lòng thử lại.");
            return "formCreate";  // Nếu có lỗi, trả về trang tạo tài khoản
        }
    }

    // Bill
    @GetMapping("/bill/{id}")
    public String showBill(@PathVariable Long id,
                           Model model) {

        BillClass bill = billrepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bill"));

        model.addAttribute("bill", bill);

        return "fromBill";
    }

    // function forgot password
    @Autowired
    ResetService resetser;

    @GetMapping("/forgot")
    public String fromForgot() {
        return "forgotPassword";
    }

    @PostMapping("/forgot")
    public String sendOtp(@RequestParam String email, Model model) {
        try {
            resetser.guiOTP(email);
            model.addAttribute("email", email);
            return "resetPassword";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "forgotPassword";
        }
    }

    @GetMapping("/reset")
    public String resetPage() {
        return "resetPassword";
    }

    @PostMapping("/reset")
    public String fromReset(@RequestParam String otp,
                            @RequestParam String password,
                            Model model) {
        try {
            resetser.resetPassword(otp, password);
            model.addAttribute("message", "Đổi mật khẩu thành công");
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "resetPassword";
        }
    }

    //function QR payment
    @GetMapping("/qr")
    public String qrPage() {
        return "qrScan";
    }

    // ===============================
    // API QUÉT QR
    // ===============================
    @PostMapping("/scanQR")
    @ResponseBody
    public Map<String, String> scanQR(@RequestBody Map<String, String> body) {
        String qr = body.get("qr");
        Map<String, String> result = new HashMap<>();
        try {
            // QR mẫu: ACC002|Nguyen Van A
            String[] data = qr.split("\\|");
            result.put("toAccount", data.length > 0 ? data[0] : "");
            result.put("accountName", data.length > 1 ? data[1] : "");
            result.put("amount", data.length > 2 ? data[2] : "");
            result.put("description", data.length > 3 ? data[3] : "");
            result.put("status", "success");

        } catch (Exception e) {

            result.put("status", "error");
            result.put("message", "QR không hợp lệ");
        }
        return result;
    }

    @GetMapping("/getUserByCode")
    @ResponseBody
    public String getUserByCode(@RequestParam String username) {

        return accountRepository.findByCode(username).map(acc -> acc.getUser().getFullName()).orElse("Tài khoản này không tồn tại trong hệ thống");
    }
    // test spi findCode
//    @GetMapping("/getUserByCode")
//    @ResponseBody
//    public String test() {
//        return "OK";
//    }

    // create myQR
    @GetMapping(value = "/myQR", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public byte[] myQR(@AuthenticationPrincipal CustomUserDetails user) throws Exception {

        AccountClass acc = accountRepository.findByUserId(user.getUserId()).orElseThrow(() -> new RuntimeException("Account not found"));
        String data = acc.getCode();

        QRCodeWriter writer = new QRCodeWriter();

        BitMatrix matrix =
                writer.encode(data,
                        BarcodeFormat.QR_CODE,
                        300, 300);

        ByteArrayOutputStream out =
                new ByteArrayOutputStream();

        MatrixToImageWriter.writeToStream(
                matrix, "PNG", out);

        return out.toByteArray();
    }

    // --- Admin Face Verify Endpoints ---
    @GetMapping("/admin/face-verify")
    public String adminFaceVerifyPage() {
        return "adminFaceVerify";
    }

    @PostMapping("/admin/face-verify/execute")
    @ResponseBody
    public Map<String, Object> executeAdminFaceVerify(@RequestBody Map<String, String> body, @AuthenticationPrincipal CustomUserDetails userDetails, HttpSession session, jakarta.servlet.http.HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            UserClass user = userrepo.findByUsername(userDetails.getUsername()).orElseThrow();
            boolean isVerified = faceIdService.verifyFace(user, body.get("image"));

            if (isVerified) {
                session.setAttribute("admin_face_verified", true);
                response.put("success", true);
                response.put("redirect", "/admin/dashboard");
            } else {
                request.logout(); // Invalidate security context
                session.invalidate();
                response.put("success", false);
                response.put("message", "Xác thực khuôn mặt thất bại.");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }
}