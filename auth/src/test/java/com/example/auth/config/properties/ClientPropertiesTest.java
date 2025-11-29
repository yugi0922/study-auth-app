package com.example.auth.config.properties;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ClientProperties.java が application.yml の設定値を正しくバインドしているか検証するテスト
 */
class ClientPropertiesTest { 

    // 💡 ベストプラクティス: ApplicationContextRunnerを使用
    // ClientPropertiesのバインド設定のみを行うコンテキストランナー
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(EnablePropertiesConfig.class); // テスト対象のPropertiesを有効化するヘルパークラスを組み込む

    @Test
    void nextjsClientIdIsBoundCorrectly() {
        contextRunner
            // 💡 テスト用のプロパティ値をセット
            .withPropertyValues(
                "auth.client.nextjs.id=test-nextjs-client-id",
                "auth.client.nextjs.redirect-uri=http://localhost:3000/test-client-callback"
            )
            .run(context -> {
                // 💡 コンテキスト起動後の検証フェーズ
                
                // 1. ClientProperties が Bean として登録されているか確認
                assertThat(context).hasSingleBean(ClientProperties.class);

                // 2. 登録された Bean を取得し、値の検証を行う
                ClientProperties properties = context.getBean(ClientProperties.class);
                
                // 3. クライアントIDの検証
                String clientId = properties.getNextjs().getId();
                assertThat(clientId)
                        .as("Client ID should be bound from properties")
                        .isEqualTo("test-nextjs-client-id");
            });
    }

    @Test
    void nextjsRedirectUriIsBoundCorrectly() {
        contextRunner
            .withPropertyValues(
                "auth.client.nextjs.id=test-client-id-uri",
                "auth.client.nextjs.redirect-uri=http://localhost:3000/test-client-callback-uri"
            )
            .run(context -> {
                // 💡 Bean を取得
                ClientProperties properties = context.getBean(ClientProperties.class);
                
                // 4. リダイレクトURIの検証
                String redirectUri = properties.getNextjs().getRedirectUri();
                assertThat(redirectUri)
                        .as("Redirect URI should be bound from properties")
                        .isEqualTo("http://localhost:3000/test-client-callback-uri");
            });
    }
    
    // ヘルパー設定クラス: テスト対象のPropertiesを有効化する (ConfigurationとEnableをまとめる)
    @Configuration
    @EnableConfigurationProperties(ClientProperties.class)
    static class EnablePropertiesConfig {
    }
}