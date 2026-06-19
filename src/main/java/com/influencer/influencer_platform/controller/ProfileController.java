package com.influencer.influencer_platform.controller;

import com.influencer.influencer_platform.dto.response.BrandProfileDto;
import com.influencer.influencer_platform.dto.response.InfluencerProfileDto;
import com.influencer.influencer_platform.dto.response.SocialMediaLinkDto;
import com.influencer.influencer_platform.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/brand/{userId}")
    public ResponseEntity<BrandProfileDto> getBrandProfile(@PathVariable Long userId, Authentication authentication) {
        BrandProfileDto response = profileService.getBrandProfile(userId, authentication);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/influencer/{userId}")
    public ResponseEntity<InfluencerProfileDto> getInfluencerProfile(@PathVariable Long userId, Authentication authentication) {
        InfluencerProfileDto response = profileService.getInfluencerProfile(userId, authentication);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/brand")
    @PreAuthorize("hasRole('BRAND')")
    public ResponseEntity<BrandProfileDto> updateBrandProfile(@Valid @RequestBody BrandProfileDto request, Authentication authentication) {
        BrandProfileDto response = profileService.updateBrandProfile(request, authentication);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/influencer")
    @PreAuthorize("hasRole('INFLUENCER')")
    public ResponseEntity<InfluencerProfileDto> updateInfluencerProfile(@Valid @RequestBody InfluencerProfileDto request, Authentication authentication) {
        InfluencerProfileDto response = profileService.updateInfluencerProfile(request, authentication);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/influencer/social-links")
    @PreAuthorize("hasRole('INFLUENCER')")
    public ResponseEntity<SocialMediaLinkDto> addSocialMediaLink(@Valid @RequestBody SocialMediaLinkDto request, Authentication authentication) {
        SocialMediaLinkDto response = profileService.addSocialMediaLink(request, authentication);
        return ResponseEntity.status(201).body(response);
    }

    @DeleteMapping("/influencer/social-links/{id}")
    @PreAuthorize("hasRole('INFLUENCER')")
    public ResponseEntity<Void> deleteSocialMediaLink(@PathVariable Long id, Authentication authentication) {
        profileService.deleteSocialMediaLink(id, authentication);
        return ResponseEntity.noContent().build();
    }
}
