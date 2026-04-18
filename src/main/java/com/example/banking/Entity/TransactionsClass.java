package com.example.banking.Entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table (name = "transactions")
public class TransactionsClass {
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public AccountClass getFrom_account_id() {
        return from_account_id;
    }

    public void setFrom_account_id(AccountClass from_account_id) {
        this.from_account_id = from_account_id;
    }

    public AccountClass getTo_account_id() {
        return to_account_id;
    }

    public void setTo_account_id(AccountClass to_account_id) {
        this.to_account_id = to_account_id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne
    @JoinColumn(name = "from_account_id")
    private AccountClass from_account_id;
    @ManyToOne
    @JoinColumn(name = "to_account_id")
    private AccountClass to_account_id;
    private BigDecimal amount;
    private String type;
    private LocalDateTime date;
    private  String description;
    private  String status;
    public TransactionsClass(){}
}
