package com.example.banking.Controller;

import com.example.banking.DTO.BillDTO;
import com.example.banking.DTO.RegisterDTO;
import com.example.banking.DTO.TransectionDTO;
import com.example.banking.Entity.AccountClass;
import com.example.banking.Entity.BillClass;
import com.example.banking.Repository.AccountRepository;
import com.example.banking.Repository.BillRepository;
import com.example.banking.Repository.TransactionRepository;
import com.example.banking.Security.CustomUserDetails;
import com.example.banking.Service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/dashboard")
    public String home(Model model,
                       @AuthenticationPrincipal CustomUserDetails userDetails) {

        String code = userDetails.getAccountNumber();

        AccountClass user = accountRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        model.addAttribute("user", user);

        // transaction history
        model.addAttribute("list",
                transactionRepository.findTop5ByFromAccountOrderByDateDesc(user));

        return "dashboard";
    }

    @GetMapping("/banking")
    public String showForm(Model model, @AuthenticationPrincipal CustomUserDetails user) {
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

    @GetMapping("/create")
    public String fromCreate(Model model) {
        model.addAttribute("dto", new RegisterDTO());
        return "formCreate";
    }

    @PostMapping("/create")
    public String Register_in_bank(@ModelAttribute RegisterDTO dto, Model model) {
        if (accountRepository.findByCode(dto.getCode()).isPresent()) {
            model.addAttribute("error", "Code đã tồn tại!!!!");
            return "formCreate";
        }

        AccountClass acc = new AccountClass();
        acc.setCode(dto.getCode());
        acc.setPassword(passwordEncoder.encode(dto.getPassword()));
        acc.setName(dto.getName());
        acc.setBalance(new BigDecimal(dto.getBalance()));
        acc.setCurrency(dto.getCurrency());
        accountRepository.save(acc);
        model.addAttribute("success", "Bạn đã đăng kí thành công!!!");
        return "redirect:/login";
    }

    @GetMapping("/bill/{id}")
    public String showBill(@PathVariable Long id,
                           Model model) {

        BillClass bill = billrepo.findById(id.intValue())
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
            // QR mẫu:
            // ACC002|50|Thanh toan
            String[] data = qr.split("\\|");
            result.put("toAccount", data[0]);
            if (data.length > 1)
                result.put("amount", data[1]);
            else
                result.put("amount", "");

            if (data.length > 2)
                result.put("description", data[2]);
            else
                result.put("description", "");

            result.put("status", "success");

        } catch (Exception e) {

            result.put("status", "error");
            result.put("message", "QR không hợp lệ");
        }
        return result;
    }

}