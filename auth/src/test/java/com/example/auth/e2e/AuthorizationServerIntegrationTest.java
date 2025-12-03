package com.example.auth.e2e;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class AuthorizationServerIntegrationTest {

    @Container
    static GenericContainer<?> authServer =
            new GenericContainer<>("auth:0.0.1-SNAPSHOT")
                    .withExposedPorts(8081)
                    .waitingFor(
                            Wait.forHttp("/.well-known/openid-configuration")
                                    .forStatusCode(200)
                                    .withStartupTimeout(Duration.ofSeconds(30))
                    );

    private static final WebClient client = WebClient.builder().build();

    private static String generateCodeVerifier() {
        byte[] buf = new byte[32];
        new SecureRandom().nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }

    private static String generateCodeChallenge(String verifier) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashed = md.digest(verifier.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashed);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String extractSessionId(List<String> cookies) {
        return cookies.stream()
                .filter(c -> c.startsWith("JSESSIONID"))
                .findFirst()
                .map(c -> c.split(";", 2)[0])
                .orElseThrow(() -> new IllegalStateException("No JSESSIONID"));
    }

    @Test
    void step1_authorizeRedirectsToLogin() {

        int port = authServer.getMappedPort(8081);
        String baseUrl = "http://localhost:" + port;

        String codeVerifier = generateCodeVerifier();
        String codeChallenge = generateCodeChallenge(codeVerifier);
        String state = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(UUID.randomUUID().toString().getBytes());

        String authorizeUrl = UriComponentsBuilder
                .fromUriString(baseUrl + "/oauth2/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", "nextjs-client")
                .queryParam("redirect_uri", "http://localhost:3000/callback")
                .queryParam("scope", "openid")
                .queryParam("code_challenge", codeChallenge)
                .queryParam("code_challenge_method", "S256")
                .queryParam("state", state)
                .toUriString();

        System.out.println("🔗 authorizeUrl = " + authorizeUrl);

        ResponseEntity<String> resp = client.get()
                .uri(authorizeUrl)
                .accept(MediaType.TEXT_HTML)
                .exchangeToMono(r -> r.toEntity(String.class))
                .block();

        assertThat(resp).isNotNull();
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FOUND);

        var redirectUri = resp.getHeaders().getLocation();
        System.out.println("📌 location = " + redirectUri);

        assertThat(redirectUri).isNotNull();
        assertThat(redirectUri.toASCIIString()).contains("/login");

        List<String> cookies = resp.getHeaders().get(HttpHeaders.SET_COOKIE);
        System.out.println("📌 set-cookie = " + cookies);
        assertThat(cookies).isNotEmpty();

        String jsessionId = extractSessionId(cookies);
        System.out.println("📌 jsessionId = " + jsessionId);
    }

    @Test
    void step2_loginAndGetCode() {

        int port = authServer.getMappedPort(8081);
        String baseUrl = "http://localhost:" + port;

        String codeVerifier = generateCodeVerifier();
        String codeChallenge = generateCodeChallenge(codeVerifier);
        String state = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(UUID.randomUUID().toString().getBytes());

        String authorizeUrl = UriComponentsBuilder
                .fromUriString(baseUrl + "/oauth2/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", "nextjs-client")
                .queryParam("redirect_uri", "http://localhost:3000/callback")
                .queryParam("scope", "openid")
                .queryParam("code_challenge", codeChallenge)
                .queryParam("code_challenge_method", "S256")
                .queryParam("state", state)
                .toUriString();

        System.out.println("\n=== STEP2 ===");
        System.out.println("authorizeUrl = " + authorizeUrl);

        var authResp = client.get()
                .uri(authorizeUrl)
                .accept(MediaType.TEXT_HTML)
                .exchangeToMono(r -> r.toEntity(String.class))
                .block();

        assertThat(authResp.getStatusCode()).isEqualTo(HttpStatus.FOUND);

        URI loginUri = authResp.getHeaders().getLocation();
        assertThat(loginUri).isNotNull();
        String loginUrl = loginUri.toASCIIString();
        System.out.println("loginUrl = " + loginUrl);

        List<String> cookies = authResp.getHeaders().get(HttpHeaders.SET_COOKIE);
        String jsessionId = extractSessionId(cookies);
        System.out.println("jsessionId = " + jsessionId);

        String html = client.get()
                .uri(loginUrl)
                .header(HttpHeaders.COOKIE, jsessionId)
                .exchangeToMono(r -> r.bodyToMono(String.class))
                .block();

        assertThat(html).isNotNull();
        String csrf = html.split("name=\"_csrf\" value=\"")[1].split("\"")[0];
        System.out.println("_csrf = " + csrf);

        var loginResp = client.post()
                .uri(loginUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header(HttpHeaders.COOKIE, jsessionId)
                .bodyValue("username=user&password=password&_csrf=" + csrf)
                .exchangeToMono(r -> r.toEntity(String.class))
                .block();

        assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.FOUND);

        URI continueUri = loginResp.getHeaders().getLocation();
        assertThat(continueUri).isNotNull();
        String continueUrl = continueUri.toASCIIString();
        System.out.println("continueUrl = " + continueUrl);
        assertThat(continueUrl).contains("continue");

        var authResp2 = client.get()
                .uri(continueUrl)
                .header(HttpHeaders.COOKIE, jsessionId)
                .exchangeToMono(r -> r.toEntity(String.class))
                .block();

        assertThat(authResp2.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        URI callbackUrl = authResp2.getHeaders().getLocation();
        assertThat(callbackUrl).isNotNull();

        String cb = callbackUrl.toASCIIString();
        System.out.println("callbackUrl = " + cb);

        assertThat(cb).contains("code=");
        assertThat(cb).contains("state=" + state);
    }
}
