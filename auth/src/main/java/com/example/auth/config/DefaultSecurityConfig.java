package com.example.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class DefaultSecurityConfig {

    // ==========================================================
    // 🔹 DevTools や Next.js が勝手に叩く URL を「認証対象外」にする設定
    // ==========================================================
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers("/.well-known/appspecific/**");  // ← 認証フィルタを完全スキップ
    }

    // ==========================================================
    // 🔹 通常の Web アプリ用 SecurityFilterChain（SAS 以外のURL向け）
    // ==========================================================
    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // 🌟 修正: ループ回避のため、/error ページへのアクセスを許可 (permitAll) にする
                .requestMatchers("/error").permitAll()
                .anyRequest().authenticated()
            )
            // 🌟 ログイン機能はここで有効化されます
            .formLogin(Customizer.withDefaults()); // 普通のフォームログイン
        return http.build();
    }
    
}