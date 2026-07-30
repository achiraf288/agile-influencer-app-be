package com.influencer.influencer_platform.controller;
import com.influencer.influencer_platform.dto.request.LoginRequest;
import com.influencer.influencer_platform.dto.request.RegisterRequest;
import com.influencer.influencer_platform.dto.response.AuthResponse;
import com.influencer.influencer_platform.dto.response.BrandProfileDto;
import com.influencer.influencer_platform.dto.response.InfluencerProfileDto;
import com.influencer.influencer_platform.dto.response.ProfileResponse;
import com.influencer.influencer_platform.dto.request.RegisterInfluencerStep3Request;
import com.influencer.influencer_platform.service.AuthService;
import com.influencer.influencer_platform.service.ProfileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final ProfileService profileService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest servletRequest, HttpServletResponse response) {
        AuthResponse authResponse = authService.register(request);

        boolean isSecure = servletRequest.isSecure() || "https".equalsIgnoreCase(servletRequest.getScheme());

        // Build cookie attributes conditionally (Secure and SameSite) to support local http dev
        String sameSite = isSecure ? "None" : "Lax";
        String secureFlag = isSecure ? "; Secure" : "";

        // Set HttpOnly cookie with JWT token
        response.addHeader("Set-Cookie", String.format(
                "jwt=%s; HttpOnly; SameSite=%s; Path=/; Max-Age=86400%s",
                authResponse.getToken(), sameSite, secureFlag
        ));
        
        AuthResponse responseWithoutToken = AuthResponse.builder()
                .role(authResponse.getRole())
                .email(authResponse.getEmail())
                .userId(authResponse.getUserId())
                .build();
        
        return ResponseEntity.status(201).body(responseWithoutToken);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest, HttpServletResponse response) {
        AuthResponse authResponse = authService.login(request);

        boolean isSecure = servletRequest.isSecure() || "https".equalsIgnoreCase(servletRequest.getScheme());

        String sameSite = isSecure ? "None" : "Lax";
        String secureFlag = isSecure ? "; Secure" : "";

        // Set HttpOnly cookie with JWT token
        response.addHeader("Set-Cookie", String.format(
                "jwt=%s; HttpOnly; SameSite=%s; Path=/; Max-Age=86400%s",
                authResponse.getToken(), sameSite, secureFlag
        ));
        
        AuthResponse responseWithoutToken = AuthResponse.builder()
                .role(authResponse.getRole())
                .email(authResponse.getEmail())
                .userId(authResponse.getUserId())
                .build();
        
        return ResponseEntity.ok(responseWithoutToken);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest servletRequest, HttpServletResponse response) {
        boolean isSecure = servletRequest.isSecure() || "https".equalsIgnoreCase(servletRequest.getScheme());
        String sameSite = isSecure ? "None" : "Lax";
        String secureFlag = isSecure ? "; Secure" : "";

        response.addHeader("Set-Cookie", String.format("jwt=; HttpOnly; SameSite=%s; Path=/; Max-Age=0%s", sameSite, secureFlag));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMe(Authentication authentication) {
        ProfileResponse response = profileService.getMyProfile(authentication);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/register/influencer/step1", consumes = {"multipart/form-data"})
    public ResponseEntity<InfluencerProfileDto> registerInfluencerStep1(
            @RequestParam String username,
            @RequestParam String niche,
            @RequestParam(required = false) String bio,
            @RequestPart(required = false, name = "profile_pic") MultipartFile profilePic,
            @RequestParam(required = false) Long userId,
            Authentication authentication) {
        return ResponseEntity.ok(profileService.completeInfluencerStep1(username, niche, bio, profilePic, userId, authentication));
    }

    @PostMapping(value = "/register/influencer/step3", consumes = {"application/json"})
    public ResponseEntity<InfluencerProfileDto> registerInfluencerStep3(
            @Valid @RequestBody RegisterInfluencerStep3Request request,
            Authentication authentication) {
        return ResponseEntity.ok(profileService.completeInfluencerStep3(request, authentication));
    }

    @PostMapping(value = "/register/brand/step2", consumes = {"multipart/form-data"})
    public ResponseEntity<BrandProfileDto> registerBrandStep2(
            @RequestParam String company_name,
            @RequestParam(required = false) String industry,
            @RequestParam(required = false) String website,
            @RequestParam(required = false) String bio,
            @RequestPart(required = false, name = "logo") MultipartFile logo,
            @RequestParam(required = false) Long userId,
            Authentication authentication) {
        return ResponseEntity.ok(profileService.completeBrandStep2(company_name, industry, website, bio, logo, userId, authentication));
    }
}
