package com.example.auth.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

// auth.client プレフィックスの設定値をこのクラスにバインド
@ConfigurationProperties(prefix = "auth.client")
public class ClientProperties {
    private NextJsClient nextjs;

    // --- Getter and Setter ---
    public NextJsClient getNextjs() { return nextjs; }
    public void setNextjs(NextJsClient nextjs) { this.nextjs = nextjs; }
    
    // 内部クラス: Next.js クライアントの特定の設定を保持
    public static class NextJsClient {
        private String id;
        private String redirectUri;

        // --- Getter and Setter ---
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getRedirectUri() { return redirectUri; }
        public void setRedirectUri(String redirectUri) { this.redirectUri = redirectUri; }
    }
}