package com.example.banking.DTO;


import java.math.BigDecimal;

public class TransectionDTO {
    private String From_account_id;
    private String To_account_id;
    private BigDecimal amount;
    private String description;

    public TransectionDTO() {
    }

    public String getFrom_account_id() {
        return From_account_id;
    }

    public void setFrom_account_id(String from_account_id) {
        From_account_id = from_account_id;
    }

    public String getTo_account_id() {
        return To_account_id;
    }

    public void setTo_account_id(String to_account_id) {
        To_account_id = to_account_id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
