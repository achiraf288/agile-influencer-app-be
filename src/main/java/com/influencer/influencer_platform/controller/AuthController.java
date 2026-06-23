package com.influencer.influencer_platform.controller;

import com.influencer.influencer_platform.dto.request.LoginRequest;
import com.influencer.influencer_platform.dto.request.RegisterRequest;
import com.influencer.influencer_platform.dto.response.AuthResponse;
import com.influencer.influencer_platform.dto.response.ProfileResponse;
import com.influencer.influencer_platform.service.AuthService;
import com.influencer.influencer_platform.service.ProfileService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final ProfileService profileService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request, HttpServletResponse response) {
        AuthResponse authResponse = authService.register(request);
        
        // Set HttpOnly cookie with JWT token
        response.addHeader("Set-Cookie", String.format(
                "jwt=%s; HttpOnly; SameSite=None; Path=/; Max-Age=86400; Secure",
                authResponse.getToken()
        ));
        
        AuthResponse responseWithoutToken = AuthResponse.builder()
                .role(authResponse.getRole())
                .email(authResponse.getEmail())
                .userId(authResponse.getUserId())
                .build();
        
        return ResponseEntity.status(201).body(responseWithoutToken);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        AuthResponse authResponse = authService.login(request);
        
        // Set HttpOnly cookie with JWT token
        response.addHeader("Set-Cookie", String.format(
                "jwt=%s; HttpOnly; SameSite=None; Path=/; Max-Age=86400; Secure",
                authResponse.getToken()
        ));
        
        AuthResponse responseWithoutToken = AuthResponse.builder()
                .role(authResponse.getRole())
                .email(authResponse.getEmail())
                .userId(authResponse.getUserId())
                .build();
        
        return ResponseEntity.ok(responseWithoutToken);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        response.addHeader("Set-Cookie", "jwt=; HttpOnly; SameSite=None; Path=/; Max-Age=0; Secure");
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMe(Authentication authentication) {
        ProfileResponse response = profileService.getMyProfile(authentication);
        return ResponseEntity.ok(response);
    }
}
