package com.influencer.influencer_platform.controller;
//editorconfig: set filetype=java

import com.influencer.influencer_platform.dto.request.BidRequest;
import com.influencer.influencer_platform.dto.response.BidResponse;
import com.influencer.influencer_platform.service.BidService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bids")
@RequiredArgsConstructor
public class BidController {

    private final BidService bidService;

    @PostMapping
    @PreAuthorize("hasRole('INFLUENCER')")
    public ResponseEntity<BidResponse> createBid(@Valid @RequestBody BidRequest request, Authentication authentication) {
        BidResponse response = bidService.createBid(request, authentication);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/campaign/{campaignId}")
    @PreAuthorize("hasRole('BRAND')")
    public ResponseEntity<List<BidResponse>> getBidsByCampaign(@PathVariable Long campaignId, Authentication authentication) {
        List<BidResponse> response = bidService.getBidsByCampaign(campaignId, authentication);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('INFLUENCER')")
    public ResponseEntity<List<BidResponse>> getMyBids(Authentication authentication) {
        List<BidResponse> response = bidService.getMyBids(authentication);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/accept")
    @PreAuthorize("hasRole('BRAND')")
    public ResponseEntity<BidResponse> acceptBid(@PathVariable Long id, Authentication authentication) {
        BidResponse response = bidService.acceptBid(id, authentication);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('BRAND')")
    public ResponseEntity<BidResponse> rejectBid(@PathVariable Long id, Authentication authentication) {
        BidResponse response = bidService.rejectBid(id, authentication);
        return ResponseEntity.ok(response);
    }
}
