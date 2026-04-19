package com.example.banking.DTO;

public class LoginDTO {
    private String code;
    private String password;

    public String getCode() {
        return code;
    }

    public LoginDTO() {
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
