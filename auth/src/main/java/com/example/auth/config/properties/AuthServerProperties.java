package com.example.auth.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

// auth.server プレフィックスの設定値をこのクラスにバインド
@ConfigurationProperties(prefix = "auth.server")
public class AuthServerProperties {
    private String issuer;
    private int accessTokenTtlHours;
    private int refreshTokenTtlDays;

    // --- Getter and Setter ---
    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }

    public int getAccessTokenTtlHours() { return accessTokenTtlHours; }
    public void setAccessTokenTtlHours(int accessTokenTtlHours) { this.accessTokenTtlHours = accessTokenTtlHours; }

    public int getRefreshTokenTtlDays() { return refreshTokenTtlDays; }
    public void setRefreshTokenTtlDays(int refreshTokenTtlDays) { this.refreshTokenTtlDays = refreshTokenTtlDays; }
}