package com.example.auth.config.properties;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner; // ① 必要なライブラリのインポート
import static org.assertj.core.api.Assertions.assertThat;

class AuthServerPropertiesTest {

    // 💡 ベストプラクティス: ApplicationContextRunnerを使用
    // 必要な設定だけを読み込む軽量なコンテキストランナー
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(EnablePropertiesConfig.class); // ② テスト対象のPropertiesを有効化する設定クラスを組み込む

    @Test
    void propertiesAreBoundCorrectly() {
        contextRunner
            // ③ テスト用のプロパティ値をセット
            // application.yml に書くのと同じ形式で、検証したい値を指定する
            .withPropertyValues(
                "auth.server.issuer=http://localhost:8081/test-runner",
                "auth.server.access-token-ttl-hours=5",
                "auth.server.refresh-token-ttl-days=30"
            )
            .run(context -> {
                // ④ コンテキスト起動後の検証フェーズ (アサーション)

                // 4-1. AuthServerProperties が Bean として登録されているか確認
                assertThat(context).hasSingleBean(AuthServerProperties.class);
                
                // 4-2. 登録された Bean を取得
                AuthServerProperties properties = context.getBean(AuthServerProperties.class);

                // 4-3. 設定値（Issuer URL）が正しく Java フィールドに注入されていることを検証
                assertThat(properties.getIssuer()).isEqualTo("http://localhost:8081/test-runner");
                
                // 4-4. 設定値（TTL: 5時間）が数値として正しく注入されていることを検証
                assertThat(properties.getAccessTokenTtlHours()).isEqualTo(5);
                
                // 4-5. 設定値（TTL: 30日）が数値として正しく注入されていることを検証
                assertThat(properties.getRefreshTokenTtlDays()).isEqualTo(30);
            });
    }

    // ⑤ ヘルパー設定クラス: テスト対象のPropertiesを有効化する
    // ApplicationContextRunner の中で AuthServerProperties を Spring に認識させるためのクラス
    @EnableConfigurationProperties(AuthServerProperties.class)
    static class EnablePropertiesConfig {
    }
}