package com.example.banking.Service;

import com.example.banking.DTO.BillDTO;
import com.example.banking.DTO.LoginDTO;
import com.example.banking.DTO.TransectionDTO;
import com.example.banking.Entity.AccountClass;
import com.example.banking.Entity.BillClass;
import com.example.banking.Repository.AccountRepository;
import com.example.banking.Repository.BillRepository;
import com.example.banking.Repository.TransactionRepository;
import com.example.banking.Repository.UserRepository;
import com.example.banking.Security.CustomUserDetails;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.banking.Entity.TransactionsClass;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AccountService {

    @Autowired
    AccountRepository accrepo;

    @Autowired
    TransactionRepository trainrepo;

    @Autowired
    BillRepository billrepo;

    @Autowired
    UserRepository userrepo;

    @Transactional

    public BillClass transferMoney(TransectionDTO request, CustomUserDetails user, BillDTO billDTO) {
        String tmp = user.getUsername();

        // 1. Tìm tài khoản (JPA sẽ tự động ánh xạ cột version vào @Version)
        AccountClass fromAcc = accrepo.findByCode(tmp).orElseThrow(() -> new RuntimeException("không tìm thấy người dùng này"));
        AccountClass toAcc = accrepo.findByCode(request.getTo_account_id()).orElseThrow(() -> new RuntimeException("không tìm thấy người dùng"));
        //Chuyển tiền phải lớn hơn 0
        if (request.getAmount().compareTo(BigDecimal.valueOf(2)) <= 0)
            throw new RuntimeException("Số tiền chuyển phải lớn hơn 2.000đ");

        // *** chặn ko cho chuyển chính account hiện tại
        if (user.getUsername().equalsIgnoreCase(toAcc.getCode())) {
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
        log.setFromAccount(fromAcc);
        log.setToAccount(toAcc);
        log.setType("Transfer");
        log.setAmount(request.getAmount());
        log.setCreated(LocalDateTime.now());
        log.setDescription(request.getDescription());
        log.setStatus("SUCCESS");
        trainrepo.save(log);

        //6. Lưu và show bill
        BillClass bill = new BillClass();
        bill.setAccountClass(fromAcc);
        bill.setTransaction_id(log);
        bill.setAmount(request.getAmount());
        bill.setBill_date(LocalDateTime.now());
        bill.setDescription(billDTO.getDescription());
        billrepo.save(bill);

        return bill;
    }
}
