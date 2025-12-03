package com.example.auth.config;

import com.example.auth.config.properties.AuthServerProperties;
import com.example.auth.config.properties.ClientProperties;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

@Configuration
// application.yml の auth-server, client の設定値をバインドして利用可能にする
@EnableConfigurationProperties({AuthServerProperties.class, ClientProperties.class})
public class AuthorizationServerConfig {

    @Autowired
    private AuthServerProperties authServerProperties; // issuer設定取得用

    @Autowired
    private RegisteredClientRepository registeredClientRepository; // クライアント情報リポジトリ

    // =========================================================================
    // 🔹 Authorization Server 専用の SecurityFilterChain
    // =========================================================================
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE) // Resource Serverより優先度を上げる必要がある
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http)
            throws Exception {

        // SAS が提供するエンドポイント群(OIDC含む)を管理する設定オブジェクト
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                OAuth2AuthorizationServerConfigurer.authorizationServer();

        http
            // 認可サーバーが管理するURLのみをこのFilterChainで扱う
            .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())

            // Authorization Server の設定を適用
            .with(authorizationServerConfigurer, authz -> authz
                .registeredClientRepository(registeredClientRepository) // クライアント情報をSASへ渡す
                .oidc(Customizer.withDefaults()) // OIDC(OpenID Connect)を有効化
            )

            // 全エンドポイントは認証が必要（/oauth2/authorize 等）
            .authorizeHttpRequests(authorize -> authorize
                .anyRequest().authenticated()
            )

            // 認証が必要なエンドポイントへ匿名アクセス時 → /login にリダイレクト
            .exceptionHandling(e -> e
                .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))
            )

            // 標準のフォームログインを有効化して /login 画面を提供
            .formLogin(Customizer.withDefaults());

        return http.build();
    }

    // =========================================================================
    // 🔹 Authorization Server の基本設定（issuer など）
    // =========================================================================
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                // ID Token 検証の"iss"に使われる値を設定（プロトコル上必須）
                .issuer(authServerProperties.getIssuer())
                .build();
    }

    // =========================================================================
    // 🔹 認可コード / トークン / セッション情報の保存先（今回はメモリ）
    // =========================================================================
    @Bean
    public OAuth2AuthorizationService authorizationService() {
        // 認可コード、アクセストークン等を保存
        return new InMemoryOAuth2AuthorizationService();
    }

    @Bean
    public OAuth2AuthorizationConsentService authorizationConsentService() {
        // Consent画面の許可状態を保存（スコープ同意）
        return new InMemoryOAuth2AuthorizationConsentService();
    }

    // =========================================================================
    // 🔹 JWK を /oauth2/jwks で公開 → JWT署名検証に使用される
    // =========================================================================
    @Bean
    public JWKSource<SecurityContext> jwkSource() throws Exception {
        // RSA鍵ペア自動生成（本番はKMSやJKS保管推奨）
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048); // 推奨鍵長
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        // JWK形式に変換（kid設定必須。ローテーション識別のため）
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString()) // kid自動生成
                .build();

        // JWK Set として返却
        // Resource Server がこれを取りにきてトークン署名を検証する
        JWKSet jwkSet = new JWKSet(rsaKey);
        return (selector, context) -> selector.select(jwkSet);
    }
}
