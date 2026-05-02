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
import com.example.banking.Service.AccountService;
import com.example.banking.Service.RegisterService;
import com.example.banking.Service.ResetService;
import com.example.banking.Service.UsersService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
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

    //Transection
    @GetMapping("/banking")
    public String showForm(Model model,
                           @AuthenticationPrincipal CustomUserDetails user,
                           @RequestParam(required = false) String toAccount,
                           @RequestParam(required = false) String amount,
                           @RequestParam(required = false) String description) {
        model.addAttribute("fullname", user.getFullName());
        
        model.addAttribute("toAccount", toAccount);
        model.addAttribute("amount", amount);
        model.addAttribute("description", description);

        return "fromCK";
    }

    @PostMapping("/banking")
    public String Transetion_in_banking(@ModelAttribute TransectionDTO request,
                                        @AuthenticationPrincipal CustomUserDetails user,
                                        BillDTO billdto, Model model) {
        try {
            BillClass bill = ser.transferMoney(request, user, billdto);
            return "redirect:/bill/" + bill.getId();
        } catch (Exception e) {
            model.addAttribute("message", e.getMessage());
        }
        return "fromCK";
    }

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
        return "historyTransection";
    }

    @Autowired
    RegisterService createser;

    //Create
    @GetMapping("/create")
    public String fromCreate(Model model) {
        model.addAttribute("dto", new RegisterDTO());
        return "formCreate";
    }

    @PostMapping("/create")
    public String Register_in_bank(@ModelAttribute RegisterDTO dto, Model model) {
        try {
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
        String data = acc.getCode()
                + "|" + user.getUsername();

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

}