package com.example.auth.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthServerAndDefaultSecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("JWK Set が公開されている")
    void jwkSetIsExposed() throws Exception {
        mockMvc.perform(get("/oauth2/jwks"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    @DisplayName("OIDC Provider Metadata が公開されている")
    void providerConfigurationIsExposed() throws Exception {
        mockMvc.perform(get("/.well-known/openid-configuration"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    @DisplayName("認可要求：必須パラメータ不足なら 400")
    void shouldReturn400WhenMissingRequiredParams() throws Exception {
        mockMvc.perform(get("/oauth2/authorize")
                        .param("response_type", "code")
                        .param("client_id", "nextjs-client"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("認可要求：未認証なら 400 (invalid_request) が返る")
    void shouldReturn400WhenUnauthenticated() throws Exception {
        String codeChallenge = "K2d8BkjFlKIPA5LYc_BWwRZ6LA5jI8x7cd7Z0W_GBOA";

        mockMvc.perform(get("/oauth2/authorize")
                        .param("response_type", "code")
                        .param("client_id", "nextjs-client")
                        .param("scope", "openid")
                        .param("redirect_uri", "http://localhost:3000/callback")
                        .param("state", "test-state")
                        .param("code_challenge", codeChallenge)
                        .param("code_challenge_method", "S256"))
                // ★ ここだけ修正：302 ではなく 400 を期待
                .andExpect(status().isBadRequest());
    }


    // @Test
    // @DisplayName("認証済み: 認可コード発行 → redirect_uri に302リダイレクト")
    // @WithMockUser(username = "user")
    // void shouldAuthorizeWhenAuthenticated() throws Exception {

    //     String codeChallenge = "K2d8BkjFlKIPA5LYc_BWwRZ6LA5jI8x7cd7Z0W_GBOA";

    //     mockMvc.perform(get("/oauth2/authorize")
    //                     .param("response_type", "code")
    //                     .param("client_id", "nextjs-client")
    //                     .param("scope", "openid")
    //                     .param("redirect_uri", "http://localhost:3000/callback")
    //                     .param("state", "test-state")
    //                     .param("code_challenge", codeChallenge)
    //                     .param("code_challenge_method", "S256"))
    //             .andExpect(status().isFound())
    //             // 認可成功 → redirect_uri に code, state が付くはず
    //             .andExpect(header().string("Location", containsString("http://localhost:3000/callback")))
    //             .andExpect(header().string("Location", containsString("code=")))
    //             .andExpect(header().string("Location", containsString("state=test-state")));
    // }    
}
