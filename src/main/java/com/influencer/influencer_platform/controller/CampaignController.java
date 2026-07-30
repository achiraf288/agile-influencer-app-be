package com.influencer.influencer_platform.controller;
import com.influencer.influencer_platform.dto.request.CampaignRequest;
import com.influencer.influencer_platform.dto.response.CampaignResponse;
import com.influencer.influencer_platform.enums.CampaignStatus;
import com.influencer.influencer_platform.service.CampaignService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/campaigns")
@RequiredArgsConstructor
public class CampaignController {

    private final CampaignService campaignService;

    @PostMapping
    @PreAuthorize("hasRole('BRAND')")
    public ResponseEntity<CampaignResponse> createCampaign(@Valid @RequestBody CampaignRequest request, Authentication authentication) {
        CampaignResponse response = campaignService.createCampaign(request, authentication);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CampaignResponse>> getCampaigns(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location) {
        List<CampaignResponse> response = campaignService.getOpenCampaigns(category, location);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('BRAND')")
    public ResponseEntity<List<CampaignResponse>> getMyCampaigns(Authentication authentication) {
        List<CampaignResponse> response = campaignService.getMyCampaigns(authentication);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CampaignResponse> getCampaignById(@PathVariable Long id) {
        CampaignResponse response = campaignService.getCampaignById(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('BRAND')")
    public ResponseEntity<CampaignResponse> updateCampaignStatus(
            @PathVariable Long id,
            @RequestParam CampaignStatus status,
            Authentication authentication) {
        CampaignResponse response = campaignService.updateCampaignStatus(id, status, authentication);
        return ResponseEntity.ok(response);
    }
}
