package com.example.banking.Security;

import com.example.banking.Entity.AdminClass;
import com.example.banking.Entity.UserClass;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * UserDetails dùng chung cho cả UserClass (bảng users) và AdminClass (bảng admins).
 * Dùng 2 constructor riêng biệt để phân biệt nguồn dữ liệu.
 */
public class CustomUserDetails implements UserDetails {

    // Các field chung
    private final Long id;
    private final String username;
    private final String password;
    private final String fullName;
    private final String role;
    private final String status;

    // Chỉ có ở UserClass
    private final UserClass user;

    // Chỉ có ở AdminClass
    private final AdminClass admin;

    // Constructor cho User thông thường
    public CustomUserDetails(UserClass user) {
        this.user = user;
        this.admin = null;
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.fullName = user.getFullName();
        this.role = user.getRole();
        this.status = user.getStatus();
    }

    // Constructor cho Admin
    public CustomUserDetails(AdminClass admin) {
        this.admin = admin;
        this.user = null;
        this.id = admin.getId();
        this.username = admin.getUsername();
        this.password = admin.getPassword();
        this.fullName = admin.getFullName();
        this.role = admin.getRole();
        this.status = admin.getStatus();
    }

    // ---- Helper methods ----

    public boolean isAdmin() {
        return admin != null;
    }

    /** Trả về UserClass nếu là user thường, null nếu là admin */
    public UserClass getUser() {
        return user;
    }

    /** Trả về AdminClass nếu là admin, null nếu là user thường */
    public AdminClass getAdmin() {
        return admin;
    }

    public Long getUserId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    // ---- UserDetails interface ----

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.toUpperCase()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !"LOCKED".equals(status);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return "ACTIVE".equals(status);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomUserDetails that = (CustomUserDetails) o;
        return username != null ? username.equals(that.username) : that.username == null;
    }

    @Override
    public int hashCode() {
        return username != null ? username.hashCode() : 0;
    }
}
