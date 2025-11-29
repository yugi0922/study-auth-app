package com.example.auth.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import com.example.auth.config.properties.AuthServerProperties;
import com.example.auth.config.properties.ClientProperties;

import java.time.Duration;
import java.util.UUID;

@Configuration
public class ClientConfig {
    
    // 💡 外部設定クラスを注入
    @Autowired
    private AuthServerProperties authServerProperties;
    
    @Autowired
    private ClientProperties clientProperties;

    // ==========================================================
    // 🔹 Next.js クライアントアプリの設定（client_id, redirect_uri など）
    // ==========================================================
    @Bean
    public RegisteredClientRepository registeredClientRepository(PasswordEncoder encoder) {
        
        // 外部化されたクライアント設定を取得
        ClientProperties.NextJsClient nextjsClient = clientProperties.getNextjs();

        // PKCE / 同意画面などのクライアント設定 (変更なし)
        ClientSettings clientSettings = ClientSettings.builder()
                .requireProofKey(true)
                .requireAuthorizationConsent(false)
                .build();

        // Access Token / Refresh Token の TTL（有効期間）を外部化
        TokenSettings tokenSettings = TokenSettings.builder()
                // 💡 POJOから値を取得し、Durationに変換
                .accessTokenTimeToLive(Duration.ofHours(authServerProperties.getAccessTokenTtlHours()))
                .refreshTokenTimeToLive(Duration.ofDays(authServerProperties.getRefreshTokenTtlDays()))
                .build();

        // Next.js クライアント定義
        RegisteredClient client = RegisteredClient.withId(UUID.randomUUID().toString())
                // 💡 POJOから値を取得
                .clientId(nextjsClient.getId()) 
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)

                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)

                // 💡 POJOから値を取得
                .redirectUri(nextjsClient.getRedirectUri()) 
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)

                .clientSettings(clientSettings)
                .tokenSettings(tokenSettings)
                .build();

        return new InMemoryRegisteredClientRepository(client);
    }
}