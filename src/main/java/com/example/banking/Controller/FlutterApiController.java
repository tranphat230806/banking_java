package com.example.banking.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.example.banking.DTO.BillDTO;
import com.example.banking.DTO.TransectionDTO;
import com.example.banking.Entity.AccountClass;
import com.example.banking.Entity.BillClass;
import com.example.banking.Entity.TransactionsClass;
import com.example.banking.Entity.UserClass;
import com.example.banking.Repository.AccountRepository;
import com.example.banking.Repository.TransactionRepository;
import com.example.banking.Repository.UserRepository;
import com.example.banking.Security.CustomUserDetails;
import com.example.banking.Service.AccountService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
public class FlutterApiController {

    private final UserRepository userrepo;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AccountService accountService;

    public FlutterApiController(
            UserRepository userrepo,
            AccountRepository accountRepository,
            TransactionRepository transactionRepository,
            AccountService accountService
    ) {
        this.userrepo = userrepo;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.accountService = accountService;
    }
    /**
     * Thông tin người dùng
     */
    @GetMapping("/profile")
    public Map<String, Object> profile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        UserClass user = userrepo
                .findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        AccountClass account = accountRepository
                .findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        Map<String, Object> result = new HashMap<>();

        result.put("id", user.getId());
        result.put("username", user.getUsername());
        result.put("fullName", user.getFullName());
        result.put("email", user.getEmail());
        result.put("phone", user.getPhone());
        result.put("avatar", user.getAvatar());

        result.put("accountCode", account.getCode());
        result.put("balance", account.getBalance());

        return result;
    }

    /**
     * Lấy số dư
     */
    @GetMapping("/balance")
    public Map<String, Object> balance(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        UserClass user = userrepo
                .findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        AccountClass account = accountRepository
                .findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        return Map.of(
                "accountCode", account.getCode(),
                "balance", account.getBalance()
        );
    }

    /**
     * Lịch sử giao dịch
     */
    @GetMapping("/history")
    public List<TransactionsClass> history(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        UserClass user = userrepo
                .findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        AccountClass account = accountRepository
                .findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        return transactionRepository
                .findByFromAccountOrToAccountOrderByCreatedDesc(
                        account,
                        account
                );
    }

    /**
     * Kiểm tra tài khoản người nhận
     */
    @GetMapping("/account-info")
    public Map<String, Object> accountInfo(
            @RequestParam String accountCode) {

        Map<String, Object> result = new HashMap<>();

        accountRepository.findByCode(accountCode)
                .ifPresentOrElse(account -> {

                    result.put("exists", true);
                    result.put("accountCode", account.getCode());
                    result.put("fullName",
                            account.getUser().getFullName());

                }, () -> {

                    result.put("exists", false);
                    result.put("message",
                            "Tài khoản không tồn tại");
                });

        return result;
    }

    /**
     * Chuyển tiền từ Flutter
     */
    @PostMapping("/payment")
    public Map<String, Object> payment(
            @RequestBody TransectionDTO request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Map<String, Object> result = new HashMap<>();

        try {

            BillDTO billDTO = new BillDTO();

            BillClass bill =
                    accountService.transferMoney(
                            request,
                            userDetails,
                            billDTO
                    );

            result.put("success", true);
            result.put("billId", bill.getId());

            return result;

        } catch (Exception e) {

            result.put("success", false);
            result.put("message", e.getMessage());

            return result;
        }
    }

    /**
     * 5 giao dịch gần nhất
     */
    @GetMapping("/recent-transactions")
    public List<TransactionsClass> recentTransactions(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        UserClass user = userrepo
                .findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        AccountClass account = accountRepository
                .findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        return transactionRepository
                .findTTop5ByFromAccountOrderByCreatedDesc(account);
    }

}