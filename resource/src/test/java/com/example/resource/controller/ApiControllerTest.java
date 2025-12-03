package com.example.resource.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.example.resource.config.SecurityConfig;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

/**
 * 🔍 Resource Server の Web 層テスト
 *
 * Spring Security Test を用いて Authentication 状態を再現し、
 * 実際にフィルタチェーンが動いた場合のレスポンスを検証する。
 *
 * ※Authorization Server なしで完全にテスト可能。
 */
@WebMvcTest(ApiController.class)
@Import(SecurityConfig.class)
class ApiControllerTest {

    @Autowired
    MockMvc mockMvc;

    /**
     * ✅ /public は無認証アクセス可能
     * → Resource Server 設定（permitAll）が正しく反映されているか確認
     */
    @Test
    void publicEndpoint_ShouldAllowAccessWithoutToken() throws Exception {
        mockMvc.perform(get("/public"))
                .andExpect(status().isOk())
                .andExpect(content().string("This is a public endpoint."));
    }

    /**
     * ❌ /private は無認証アクセス禁止
     * → Authorization ヘッダなし → 401 Unauthorized となることを検証
     */
    @Test
    void privateEndpoint_ShouldReturn401_WhenNoToken() throws Exception {
        mockMvc.perform(get("/private"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * 🔐 JWT 認証成功ケース
     * MockMvc に対し SecurityContext 内の Principal を直接注入することで
     * 実 JWT デコード処理なしで認証成功ルートを完全再現する。
     *
     * - subject("test-user") → Jwt.getSubject() に相当
     * - scope="openid" → 権限変換で "SCOPE_openid" が付与される
     */
    @Test
    void privateEndpoint_ShouldReturn200_WhenJwtValid() throws Exception {
        mockMvc.perform(get("/private")
                        .with(jwt().jwt(jwt -> jwt
                                .subject("test-user")
                                .claim("scope", "openid")
                        )))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, test-user! You accessed a protected resource."));
    }

    /**
     * ❌ 無効なトークン
     * Authorization ヘッダは存在するが中身が不正 → 認証失敗 → 401
     *
     * ※このケースは BearerTokenAuthenticationFilter による失敗ルートの検証
     */
    @Test
    void privateEndpoint_ShouldReturn401_WhenJwtInvalid() throws Exception {
        mockMvc.perform(get("/private")
                        .header("Authorization", "Bearer invalid"))
                .andExpect(status().isUnauthorized());
    }
}
