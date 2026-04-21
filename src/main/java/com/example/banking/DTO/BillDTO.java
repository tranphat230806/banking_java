package com.example.banking.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BillDTO {
    private String from_account_id;
    private String from_Name;
    private String to_account_id;

    public BillDTO() {
    }

    public String getFrom_account_id() {
        return from_account_id;
    }

    public void setFrom_account_id(String from_account_id) {
        this.from_account_id = from_account_id;
    }

    public String getFrom_Name() {
        return from_Name;
    }

    public void setFrom_Name(String from_Name) {
        this.from_Name = from_Name;
    }

    public String getTo_account_id() {
        return to_account_id;
    }

    public void setTo_account_id(String to_account_id) {
        this.to_account_id = to_account_id;
    }

    public String getTo_Name() {
        return to_Name;
    }

    public void setTo_Name(String to_Name) {
        this.to_Name = to_Name;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getBill_date() {
        return bill_date;
    }

    public void setBill_date(LocalDateTime bill_date) {
        this.bill_date = bill_date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private String to_Name;
    private BigDecimal amount;
    private LocalDateTime bill_date;
    private String description;

}
