package com.example.banking.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class DotenvConfig {
    static {
        Dotenv dotenv = Dotenv.load();
        System.setProperty("GMAIL_USER", dotenv.get("GMAIL_USER"));
        System.setProperty("GMAIL_APP_PASSWORD", dotenv.get("GMAIL_APP_PASSWORD"));
    }
}
