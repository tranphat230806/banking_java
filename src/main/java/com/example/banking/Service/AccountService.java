package com.example.banking.Service;

import com.example.banking.DTO.LoginDTO;
import com.example.banking.DTO.TransectionDTO;
import com.example.banking.Entity.AccountClass;
import com.example.banking.Repository.AccountRepository;
import com.example.banking.Repository.TransactionRepository;
import com.example.banking.Security.CustomUserDetails;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.banking.Entity.TransactionsClass;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AccountService {

    @Autowired
    AccountRepository accrepo;

    @Autowired
    TransactionRepository trainrepo;


    @Transactional

    public void transferMoney(TransectionDTO request, CustomUserDetails user) {
        String tmp = user.getAccountNumber();

        // 1. Tìm tài khoản (JPA sẽ tự động ánh xạ cột version vào @Version)
        AccountClass fromAcc = accrepo.findByCode(tmp).orElseThrow(() -> new RuntimeException("không tìm thấy mã code"));
        AccountClass toAcc = accrepo.findByCode(request.getTo_account_id()).orElseThrow(() -> new RuntimeException("không tìm thấy mã code"));
        // *** chặn ko cho chuyển chính account hiện tại
        if (user.getAccountNumber().equalsIgnoreCase(toAcc.getCode())) {
            throw new RuntimeException("Lỗi không thể chuyển cho chính tài khoản của bạn!!");
        }
        // 2. Kiểm tra nghiệp vụ
        if (fromAcc.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Không đủ tiền");
        }

        // 3. Thực hiện cộng trừ trên đối tượng (In-memory)
        fromAcc.setBalance(fromAcc.getBalance().subtract(request.getAmount()));
        toAcc.setBalance(toAcc.getBalance().add(request.getAmount()));

        // 4. Lưu vào DB
        // Nếu có thread khác đã thay đổi version, Hibernate sẽ ném ra ObjectOptimisticLockingFailureException
        accrepo.save(fromAcc);
        accrepo.save(toAcc);

        // 5. Ghi log giao dịch
        TransactionsClass log = new TransactionsClass();
        log.setFrom_account_id(fromAcc);
        log.setTo_account_id(toAcc);
        log.setAmount(request.getAmount());
        log.setDate(LocalDateTime.now());
        log.setDescription(request.getDescription());
        log.setStatus("SUCCESS");
        trainrepo.save(log);
    }
}
