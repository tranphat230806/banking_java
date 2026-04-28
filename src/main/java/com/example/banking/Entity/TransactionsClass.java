package com.example.banking.Entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class TransactionsClass {
    public Long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public AccountClass getFromAccount() {
        return fromAccount;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public void setFromAccount(AccountClass fromAccount) {
        this.fromAccount = fromAccount;
    }

    public AccountClass getToAccount() {
        return toAccount;
    }

    public void setToAccount(AccountClass toAccount) {
        this.toAccount = toAccount;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @ManyToOne
    @JoinColumn(name = "from_account_id")
    private AccountClass fromAccount;
    @ManyToOne
    @JoinColumn(name = "to_account_id")
    private AccountClass toAccount;
    private BigDecimal amount;
    private String type;

    @Column(name = "created")
    private LocalDateTime created;
    private String description;
    private String status;

    public TransactionsClass() {
    }
}
