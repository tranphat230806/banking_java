package com.example.banking.Controller;

import com.example.banking.DTO.TransectionDTO;
import com.example.banking.Repository.AccountRepository;
import com.example.banking.Service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Controller
public class TransectionController {
    @Autowired
    AccountService ser;

    @GetMapping("/banking")
    public String showForm() {
        return "fromCK";
    }
    @PostMapping("/banking")
    public String Transetion_in_banking( @ModelAttribute TransectionDTO request, Model model)
    {
        try {
        ser.transferMoney(request);
        model.addAttribute("message", "Chuyển Khoản Thành Công");
        }
        catch(Exception e){
            model.addAttribute("message","Bạn không đủ tiền");
        }
        return "fromCK";
    }
}