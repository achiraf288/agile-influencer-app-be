package com.influencer.influencer_platform.controller;

import com.influencer.influencer_platform.dto.response.BrandDashboardDto;
import com.influencer.influencer_platform.dto.response.BrandProfileDto;
import com.influencer.influencer_platform.dto.response.BidResponse;
import com.influencer.influencer_platform.dto.response.CampaignResponse;
import com.influencer.influencer_platform.service.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/brand")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    @GetMapping("/campaigns")
    @PreAuthorize("hasRole('BRAND')")
    public ResponseEntity<Map<String, List<CampaignResponse>>> getBrandCampaigns(Authentication authentication) {
        List<CampaignResponse> response = brandService.getBrandCampaigns(authentication);
        return ResponseEntity.ok(Map.of("campaigns", response));
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('BRAND')")
    public ResponseEntity<BrandDashboardDto> getBrandDashboard(Authentication authentication) {
        BrandDashboardDto response = brandService.getBrandDashboard(authentication);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('BRAND')")
    public ResponseEntity<BrandProfileDto> getBrandProfile(Authentication authentication) {
        BrandProfileDto response = brandService.getBrandProfile(authentication);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('BRAND')")
    public ResponseEntity<?> updateBrandProfile(
            @RequestBody BrandProfileDto request,
            Authentication authentication) {
        BrandProfileDto response = brandService.updateBrandProfile(request, authentication);
        return ResponseEntity.ok(Map.of(
                "message", "Profile updated successfully",
                "profile", response
        ));
    }

    @PostMapping("/profile/logo")
    @PreAuthorize("hasRole('BRAND')")
    public ResponseEntity<?> updateBrandLogo(
            @RequestParam("logo") MultipartFile logo,
            Authentication authentication) {
        BrandProfileDto request = BrandProfileDto.builder().build();
        BrandProfileDto response = brandService.updateBrandProfile(request, logo, authentication);
        return ResponseEntity.ok(Map.of(
                "message", "Logo updated successfully",
                "profile", response
        ));
    }

    @GetMapping("/bids")
    @PreAuthorize("hasRole('BRAND')")
    public ResponseEntity<List<BidResponse>> getBrandBids(Authentication authentication) {
        List<BidResponse> response = brandService.getBrandBids(authentication);
        return ResponseEntity.ok(response);
    }
}
