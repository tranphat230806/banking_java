package com.example.banking.Entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
public class UserClass {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String username;
    private String password;
    @Column(name = "full_name")
    private String fullName;
    private String email;
    private String phone;
    private String avatar;
    private String role = "ROLE_USER";
    private String status = "ACTIVE";
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "face_registered")
    private boolean faceRegistered = false;
    
    @Column(name = "face_image_path")
    private String faceImagePath;

    @Column(name = "transfer_pin")
    private String transferPin;

    @Column(name = "pin_fail_count")
    private int pinFailCount = 0;

    @Column(name = "transfer_locked")
    private boolean transferLocked = false;

    @Column(name = "transfer_otp")
    private String transferOtp;

    @Column(name = "transfer_otp_expiry")
    private LocalDateTime transferOtpExpiry;


    public AccountClass getAccounts() {
        return accounts;
    }

    public void setAccounts(AccountClass accounts) {
        this.accounts = accounts;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @OneToOne(mappedBy = "user")
    private AccountClass accounts;

    public UserClass() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isFaceRegistered() {
        return faceRegistered;
    }

    public void setFaceRegistered(boolean faceRegistered) {
        this.faceRegistered = faceRegistered;
    }

    public String getFaceImagePath() {
        return faceImagePath;
    }

    public void setFaceImagePath(String faceImagePath) {
        this.faceImagePath = faceImagePath;
    }

    public String getTransferPin() {
        return transferPin;
    }

    public void setTransferPin(String transferPin) {
        this.transferPin = transferPin;
    }

    public int getPinFailCount() {
        return pinFailCount;
    }

    public void setPinFailCount(int pinFailCount) {
        this.pinFailCount = pinFailCount;
    }

    public boolean isTransferLocked() {
        return transferLocked;
    }

    public void setTransferLocked(boolean transferLocked) {
        this.transferLocked = transferLocked;
    }

    public String getTransferOtp() {
        return transferOtp;
    }

    public void setTransferOtp(String transferOtp) {
        this.transferOtp = transferOtp;
    }

    public LocalDateTime getTransferOtpExpiry() {
        return transferOtpExpiry;
    }

    public void setTransferOtpExpiry(LocalDateTime transferOtpExpiry) {
        this.transferOtpExpiry = transferOtpExpiry;
    }
}
