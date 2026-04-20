package com.example.banking.Service;

import com.example.banking.Entity.AccountClass;
import com.example.banking.Repository.AccountRepository;
import com.example.banking.Security.CustomUserDetails;
import com.example.banking.config.PasswordConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsersService implements UserDetailsService {
    @Autowired
    AccountRepository repo;

    @Override
    public UserDetails loadUserByUsername(String code) throws UsernameNotFoundException {
        AccountClass acc = repo.findByCode(code).orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản này!!!"));
        return new CustomUserDetails(acc);
    }

    @Autowired
    PasswordEncoder passwordEncoder;

}
