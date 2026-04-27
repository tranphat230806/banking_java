package com.example.banking.Entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bills")
public class BillClass {
    public BillClass() {
    }


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @OneToOne
    @JoinColumn(name = "transaction_id")
    private TransactionsClass transaction_id;
    @ManyToOne
    @JoinColumn(name = "account_id")
    private AccountClass accountClass;
    private BigDecimal amount;
    private String description;
    private LocalDateTime bill_date;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public TransactionsClass getTransaction_id() {
        return transaction_id;
    }

    public void setTransaction_id(TransactionsClass transaction_id) {
        this.transaction_id = transaction_id;
    }

    public AccountClass getAccountClass() {
        return accountClass;
    }

    public void setAccountClass(AccountClass accountClass) {
        this.accountClass = accountClass;
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

    public LocalDateTime getBill_date() {
        return bill_date;
    }

    public void setBill_date(LocalDateTime bill_date) {
        this.bill_date = bill_date;
    }
}
