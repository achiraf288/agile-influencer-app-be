package com.influencer.influencer_platform.controller;

import com.influencer.influencer_platform.dto.request.BidRequest;
import com.influencer.influencer_platform.dto.request.SubmissionRequest;
import com.influencer.influencer_platform.dto.response.BidResponse;
import com.influencer.influencer_platform.dto.response.CampaignResponse;
import com.influencer.influencer_platform.dto.response.InfluencerProfileDto;
import com.influencer.influencer_platform.dto.response.SubmissionResponse;
import com.influencer.influencer_platform.service.BidService;
import com.influencer.influencer_platform.service.CampaignService;
import com.influencer.influencer_platform.service.ProfileService;
import com.influencer.influencer_platform.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/influencer")
@RequiredArgsConstructor
public class InfluencerController {

    private final ProfileService profileService;
    private final CampaignService campaignService;
    private final BidService bidService;
    private final SubmissionService submissionService;

    @GetMapping("/profile")
    @PreAuthorize("hasRole('INFLUENCER')")
    public ResponseEntity<InfluencerProfileDto> getInfluencerProfile(Authentication authentication) {
        // Get current user's ID from authentication
        Long userId = Long.parseLong(authentication.getName());
        InfluencerProfileDto response = profileService.getInfluencerProfile(userId, authentication);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('INFLUENCER')")
    public ResponseEntity<InfluencerProfileDto> updateInfluencerProfile(
            @RequestBody InfluencerProfileDto request,
            Authentication authentication) {
        InfluencerProfileDto response = profileService.updateInfluencerProfile(request, authentication);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/profile")
    @PreAuthorize("hasRole('INFLUENCER')")
    public ResponseEntity<Map<String, String>> deleteInfluencerProfile(Authentication authentication) {
        // TODO: Implement profile deletion in ProfileService
        return ResponseEntity.ok(Map.of("message", "Profile deletion not yet implemented"));
    }

    @GetMapping("/campaigns")
    @PreAuthorize("hasRole('INFLUENCER')")
    public ResponseEntity<List<CampaignResponse>> getCampaigns(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location) {
        List<CampaignResponse> response = campaignService.getOpenCampaigns(category, location);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/campaigns/{campaignId}")
    @PreAuthorize("hasRole('INFLUENCER')")
    public ResponseEntity<CampaignResponse> getCampaignById(@PathVariable Long campaignId) {
        CampaignResponse response = campaignService.getCampaignById(campaignId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/dashboard/stats")
    @PreAuthorize("hasRole('INFLUENCER')")
    public ResponseEntity<Map<String, Object>> getDashboardStats(Authentication authentication) {
        // TODO: Implement dashboard stats in service
        return ResponseEntity.ok(Map.of(
            "activeBids", 0,
            "activeAssignments", 0
        ));
    }

    @GetMapping("/dashboard/activity")
    @PreAuthorize("hasRole('INFLUENCER')")
    public ResponseEntity<List<Map<String, Object>>> getRecentActivity(Authentication authentication) {
        // TODO: Implement recent activity in service
        return ResponseEntity.ok(List.of());
    }

    // Bid Endpoints
    @GetMapping("/bids")
    @PreAuthorize("hasRole('INFLUENCER')")
    public ResponseEntity<List<BidResponse>> getInfluencerBids(Authentication authentication) {
        List<BidResponse> response = bidService.getMyBids(authentication);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/bids")
    @PreAuthorize("hasRole('INFLUENCER')")
    public ResponseEntity<BidResponse> createBid(@RequestBody BidRequest request, Authentication authentication) {
        BidResponse response = bidService.createBid(request, authentication);
        return ResponseEntity.status(201).body(response);
    }

    @PutMapping("/bids/{bidId}")
    @PreAuthorize("hasRole('INFLUENCER')")
    public ResponseEntity<BidResponse> updateBid(
            @PathVariable Long bidId,
            @RequestBody BidRequest request,
            Authentication authentication) {
        BidResponse response = bidService.updateBid(bidId, request, authentication);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/bids/{bidId}")
    @PreAuthorize("hasRole('INFLUENCER')")
    public ResponseEntity<Void> deleteBid(@PathVariable Long bidId, Authentication authentication) {
        bidService.deleteBid(bidId, authentication);
        return ResponseEntity.noContent().build();
    }

    // Assignment Endpoints
    @GetMapping("/assignments")
    @PreAuthorize("hasRole('INFLUENCER')")
    public ResponseEntity<List<Map<String, Object>>> getInfluencerAssignments(Authentication authentication) {
        // TODO: Implement assignments retrieval in service
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/assignments/{assignmentId}")
    @PreAuthorize("hasRole('INFLUENCER')")
    public ResponseEntity<Map<String, Object>> getAssignmentById(@PathVariable Long assignmentId, Authentication authentication) {
        // TODO: Implement assignment retrieval in service
        return ResponseEntity.ok(Map.of());
    }

    @PostMapping("/assignments/{assignmentId}/submissions")
    @PreAuthorize("hasRole('INFLUENCER')")
    public ResponseEntity<SubmissionResponse> createSubmission(
            @PathVariable Long assignmentId,
            @RequestBody SubmissionRequest request,
            Authentication authentication) {
        request.setAssignmentId(assignmentId);
        SubmissionResponse response = submissionService.createSubmission(request, authentication);
        return ResponseEntity.status(201).body(response);
    }

    @PutMapping("/assignments/{assignmentId}/submissions/{submissionId}")
    @PreAuthorize("hasRole('INFLUENCER')")
    public ResponseEntity<SubmissionResponse> updateSubmission(
            @PathVariable Long assignmentId,
            @PathVariable Long submissionId,
            @RequestBody SubmissionRequest request,
            Authentication authentication) {
        // TODO: Implement submission update in service
        return ResponseEntity.ok(SubmissionResponse.builder().build());
    }
}
