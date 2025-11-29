package com.example.auth.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@TestConfiguration
public class OAuth2AuthorizeDebugFilterConfig {

    @Bean
    public OncePerRequestFilter oauth2AuthorizeDebugFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(
                    HttpServletRequest request,
                    HttpServletResponse response,
                    FilterChain filterChain
            ) throws ServletException, IOException {

                if (request.getRequestURI().startsWith("/oauth2/authorize")) {
                    System.err.println("\n===== DEBUG: /oauth2/authorize parameterMap =====");
                    request.getParameterMap().forEach((k, v) ->
                        System.err.printf("%s -> %s%n", k, Arrays.toString(v))
                    );
                    System.err.println("=================================================\n");
                }

                filterChain.doFilter(request, response);
            }
        };
    }
}
