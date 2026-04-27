package com.example.banking.Controller;

import com.example.banking.DTO.BillDTO;
import com.example.banking.DTO.RegisterDTO;
import com.example.banking.DTO.TransectionDTO;
import com.example.banking.Entity.AccountClass;
import com.example.banking.Entity.BillClass;
import com.example.banking.Entity.UserClass;
import com.example.banking.Repository.AccountRepository;
import com.example.banking.Repository.BillRepository;
import com.example.banking.Repository.TransactionRepository;
import com.example.banking.Repository.UserRepository;
import com.example.banking.Security.CustomUserDetails;
import com.example.banking.Service.AccountService;
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
import java.util.HashMap;
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

        // transaction history
        model.addAttribute("list",
                transactionRepository.findTop5ByFromAccountOrderByCreatedDesc(user));

        return "dashboard";
    }

    //Transection
    @GetMapping("/banking")
    public String showForm(Model model, @AuthenticationPrincipal CustomUserDetails user, @ModelAttribute TransectionDTO tmp) {
        model.addAttribute("nameUser", user.getUsername());

        return "fromCK";
    }

    @PostMapping("/banking")
    public String Transetion_in_banking(@ModelAttribute TransectionDTO request, @AuthenticationPrincipal CustomUserDetails user, BillDTO billdto, Model model) {
        try {
            BillClass bill = ser.transferMoney(request, user, billdto);
            return "redirect:/bill/" + bill.getId();
        } catch (Exception e) {
            model.addAttribute("message", e.getMessage());
        }
        return "fromCK";
    }

    //Create
    @GetMapping("/create")
    public String fromCreate(Model model) {
        model.addAttribute("dto", new RegisterDTO());
        return "formCreate";
    }

    @PostMapping("/create")
    public String Register_in_bank(@ModelAttribute RegisterDTO dto, Model model) {
        if (userrepo.findByUsername(dto.getUsername()).isPresent()) {
            model.addAttribute("error", "Code đã tồn tại!!!!");
            return "formCreate";
        }

        UserClass user = new UserClass();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setAvatar(dto.getAvatar());
        userrepo.save(user);
        model.addAttribute("success", "Bạn đã đăng kí thành công!!!");
        return "redirect:/login";
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
        return userrepo.findByUsername(username).map(UserClass::getUsername).orElse("Tài khoản này không tồn tại trong hệ thống");
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

        String data = user.getUsername()
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