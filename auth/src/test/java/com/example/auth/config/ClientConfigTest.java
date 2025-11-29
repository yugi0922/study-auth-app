package com.example.auth.config;

import com.example.auth.config.properties.AuthServerProperties;
import com.example.auth.config.properties.ClientProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ClientConfig の設定ロジックを検証するテスト (ApplicationContextRunner使用)
 */
class ClientConfigTest {

    // 💡 テスト実行に必要な設定とプロパティを定義するランナー
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(
                    ClientConfig.class,            // テスト対象
                    UserAuthenticationConfig.class,// 依存コンポーネント
                    PropertyEnableConfig.class     // プロパティ有効化用ヘルパー
            );

    private static final String CLIENT_ID = "test-nextjs-client";

    @Test
    @DisplayName("プロパティ値が反映され、RegisteredClientが正しく生成される")
    void registeredClientIsConfiguredCorrectly() {
        contextRunner
            // 💡 テスト用プロパティをここで注入 (Duration.ZEROエラーを確実に回避)
            .withPropertyValues(
                "auth.server.access-token-ttl-hours=4",
                "auth.server.refresh-token-ttl-days=30",
                "auth.client.nextjs.id=" + CLIENT_ID,
                "auth.client.nextjs.redirect-uri=http://localhost:3000/callback"
            )
            .run(context -> {
                // 1. Beanの存在確認
                assertThat(context).hasSingleBean(RegisteredClientRepository.class);
                RegisteredClientRepository repository = context.getBean(RegisteredClientRepository.class);

                // 2. クライアントの取得と検証
                RegisteredClient client = repository.findByClientId(CLIENT_ID);
                assertThat(client).isNotNull();

                // 3. 設定値の検証
                // 認証方法
                assertThat(client.getClientAuthenticationMethods())
                        .containsExactly(ClientAuthenticationMethod.NONE);
                
                // Grant Types
                assertThat(client.getAuthorizationGrantTypes())
                        .contains(AuthorizationGrantType.AUTHORIZATION_CODE, AuthorizationGrantType.REFRESH_TOKEN);

                // Scopes
                assertThat(client.getScopes())
                        .contains(OidcScopes.OPENID, OidcScopes.PROFILE);

                // Redirect URI (プロパティ経由)
                assertThat(client.getRedirectUris())
                        .containsExactly("http://localhost:3000/callback");

                // TTL (プロパティ経由)
                assertThat(client.getTokenSettings().getAccessTokenTimeToLive())
                        .isEqualTo(Duration.ofHours(4));
                assertThat(client.getTokenSettings().getRefreshTokenTimeToLive())
                        .isEqualTo(Duration.ofDays(30));
                
                // PKCE設定
                assertThat(client.getClientSettings().isRequireProofKey()).isTrue();
            });
    }

    // 💡 ヘルパー構成: POJOプロパティを有効化するためだけのクラス
    @Configuration
    @EnableConfigurationProperties({AuthServerProperties.class, ClientProperties.class})
    static class PropertyEnableConfig {}
}