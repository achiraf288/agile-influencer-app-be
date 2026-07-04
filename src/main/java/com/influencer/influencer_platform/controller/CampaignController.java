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

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/campaigns")
@RequiredArgsConstructor
public class CampaignController {

    private final CampaignService campaignService;

    @PostMapping
    @PreAuthorize("hasRole('BRAND')")
    public ResponseEntity<Map<String, Object>> createCampaign(@Valid @RequestBody CampaignRequest request, Authentication authentication) {
        CampaignResponse response = campaignService.createCampaign(request, authentication);
        return ResponseEntity.status(201).body(Map.of(
                "message", "Campaign created successfully",
                "campaign", response
        ));
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

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('BRAND')")
    public ResponseEntity<Map<String, Object>> updateCampaign(
            @PathVariable Long id,
            @Valid @RequestBody CampaignRequest request,
            Authentication authentication) {
        CampaignResponse response = campaignService.updateCampaign(id, request, authentication);
        return ResponseEntity.ok(Map.of(
                "message", "Campaign updated successfully",
                "campaign", response
        ));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('BRAND')")
    public ResponseEntity<Map<String, String>> deleteCampaign(@PathVariable Long id, Authentication authentication) {
        campaignService.deleteCampaign(id, authentication);
        return ResponseEntity.ok(Map.of("message", "Campaign deleted successfully"));
    }
}
