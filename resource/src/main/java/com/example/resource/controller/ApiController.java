package com.example.resource.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

@RestController
public class ApiController {

    @GetMapping("/public")
    public String publicApi() {
        return "This is a public endpoint.";
    }

    @GetMapping("/private")
    public String privateApi(@AuthenticationPrincipal Jwt jwt) {
        return "Hello, " + jwt.getSubject() + "! You accessed a protected resource.";
    }
}
