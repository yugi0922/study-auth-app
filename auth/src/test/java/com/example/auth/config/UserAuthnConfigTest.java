package com.example.auth.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * UserAuthenticationConfig が UserDetailsService と PasswordEncoder を正しく定義しているかを検証するテスト。
 */
// 💡 テスト対象のクラスを指定してコンテキストをロード
@SpringBootTest(classes = UserAuthenticationConfig.class) 
@ActiveProfiles("test")
class UserAuthnConfigTest {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ==========================================================
    // 1. PasswordEncoder の検証
    // ==========================================================
    @Test
    void passwordEncoderIsBCrypt() {
        // PasswordEncoder の実装が BCryptPasswordEncoder であることを確認
        assertThat(passwordEncoder).isInstanceOf(BCryptPasswordEncoder.class);
        
        // テスト用のパスワードが正しくハッシュ化されることを確認
        String rawPassword = "password";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        // ハッシュ化されたパスワードが検証可能であるか確認
        assertThat(passwordEncoder.matches(rawPassword, encodedPassword)).isTrue();
    }
    
    // ==========================================================
    // 2. UserDetailsService の検証
    // ==========================================================
    @Test
    void testUserCanBeLoaded() {
        // 設定されたユーザー名 "user" でユーザー情報がロードできることを検証
        UserDetails userDetails = userDetailsService.loadUserByUsername("user");
        
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("user");
        
        // ユーザーが "USER" ロールを持っていることを検証
        assertThat(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER"))).isTrue(); 
    }

    @Test
    void nonExistingUserFailsToLoad() {
        // 存在しないユーザー名をロードしようとすると例外が発生することを検証
        assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername("nonexistent");
        });
    }
}