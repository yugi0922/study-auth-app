package com.example.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
public class UserAuthenticationConfig {

    // ==========================================================
    // 🔹 ログインユーザーの登録（認証で使う）: UserDetailsService の定義
    // ==========================================================
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        UserDetails user = User.builder()
                .username("user")
                .password(encoder.encode("password"))
                .roles("USER")
                .build();

        // InMemoryUserDetailsManager に保存 → 認証で使われる
        // 🚨 本番環境ではDB接続などに置き換えるための独立したコンフィグです
        return new InMemoryUserDetailsManager(user);
    }

    // ==========================================================
    // 🔹 パスワードのハッシュ化（ログイン用）: PasswordEncoder の定義
    // ==========================================================
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}