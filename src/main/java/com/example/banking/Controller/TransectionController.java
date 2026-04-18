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
import java.util.HashMap;
import java.util.Map;

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
    // ===============================
    // API QUÉT QR
    // ===============================
    @PostMapping("/scanQR")
    @ResponseBody
    public Map<String,String> scanQR(
            @RequestBody Map<String,String> body
    ) {

        String qr = body.get("qr");

        Map<String,String> result = new HashMap<>();

        try {

            // QR mẫu:
            // ACC002|50000|Thanh toan

            String[] data = qr.split("\\|");

            result.put("toAccount", data[0]);

            if(data.length > 1)
                result.put("amount", data[1]);
            else
                result.put("amount", "");

            if(data.length > 2)
                result.put("description", data[2]);
            else
                result.put("description", "");

            result.put("status", "success");

        }
        catch (Exception e){

            result.put("status", "error");
            result.put("message", "QR không hợp lệ");
        }

        return result;
    }
}