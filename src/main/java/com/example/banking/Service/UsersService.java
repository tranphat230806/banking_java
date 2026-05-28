package com.example.banking.Service;

import com.example.banking.Entity.AdminClass;
import com.example.banking.Entity.UserClass;
import com.example.banking.Repository.AdminRepository;
import com.example.banking.Repository.UserRepository;
import com.example.banking.Security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsersService implements UserDetailsService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private AdminRepository adminRepo;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Tìm trong bảng users trước
        Optional<UserClass> userOpt = userRepo.findByUsername(username);
        if (userOpt.isPresent()) {
            return new CustomUserDetails(userOpt.get());
        }

        // 2. Nếu không có, tìm trong bảng admins
        Optional<AdminClass> adminOpt = adminRepo.findByUsername(username);
        if (adminOpt.isPresent()) {
            return new CustomUserDetails(adminOpt.get());
        }

        throw new UsernameNotFoundException("Không tìm thấy tài khoản: " + username);
    }
}
