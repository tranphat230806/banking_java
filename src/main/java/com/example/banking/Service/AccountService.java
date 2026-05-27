package com.example.banking.Service;

import com.example.banking.DTO.BillDTO;
import com.example.banking.DTO.LoginDTO;
import com.example.banking.DTO.TransectionDTO;
import com.example.banking.Entity.AccountClass;
import com.example.banking.Entity.BillClass;
import com.example.banking.Entity.UserClass;
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

    public BillClass transferMoney(TransectionDTO request,
                                   CustomUserDetails user,
                                   BillDTO billDTO) {

        // 1. Lấy username từ Security
        String username = user.getUsername();

        // 2. Tìm user trước
        UserClass userEntity = userrepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        // 3. Lấy tài khoản chính của user
        AccountClass fromAcc = userEntity.getAccounts();

        // 4. Tài khoản nhận tiền (theo code nhập)
        AccountClass toAcc = accrepo.findByCode(request.getToAccountId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản nhận"));


        // 6. Không cho chuyển chính mình
        if (fromAcc.getId() == (toAcc.getId())) {
            throw new RuntimeException("Không thể chuyển cho chính tài khoản của bạn");
        }

        // 7. Check số dư
        if (request.getAmount().compareTo(BigDecimal.valueOf(2000)) <= 0) {
            throw new RuntimeException("Số tiền chuyển tối thiểu 2.000d");
        }
        if (request.getAmount().compareTo(fromAcc.getBalance()) > 0) {
            throw new RuntimeException("Số dư không đủ để thực hiện chuyển khoản");
        }

        // 8. Trừ + cộng tiền
        fromAcc.setBalance(fromAcc.getBalance().subtract(request.getAmount()));
        toAcc.setBalance(toAcc.getBalance().add(request.getAmount()));

        accrepo.save(fromAcc);
        accrepo.save(toAcc);

        // 9. Log transaction
        TransactionsClass log = new TransactionsClass();
        log.setFromAccount(fromAcc);
        log.setToAccount(toAcc);
        log.setType("TRANSFER");
        log.setAmount(request.getAmount());
        log.setCreated(LocalDateTime.now());
        log.setDescription(request.getDescription());
        log.setStatus("SUCCESS");

        trainrepo.save(log);

        // 10. Bill
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
