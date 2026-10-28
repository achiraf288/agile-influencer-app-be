package com.influencer.influencer_platform.controller;

import com.influencer.influencer_platform.dto.response.BrandProfileDto;
import com.influencer.influencer_platform.dto.response.CampaignResponse;
import com.influencer.influencer_platform.entity.Bid;
import com.influencer.influencer_platform.enums.BidStatus;
import com.influencer.influencer_platform.enums.CampaignStatus;
import com.influencer.influencer_platform.repository.BidRepository;
import com.influencer.influencer_platform.service.CampaignService;
import com.influencer.influencer_platform.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/brand")
@PreAuthorize("hasRole('BRAND')")
@RequiredArgsConstructor
public class BrandController {

    private final CampaignService campaignService;
    private final ProfileService profileService;
    private final BidRepository bidRepository;

    @GetMapping("/campaigns")
    public ResponseEntity<List<CampaignResponse>> getMyCampaigns(Authentication authentication) {
        return ResponseEntity.ok(campaignService.getMyCampaigns(authentication));
    }

    @GetMapping("/profile")
    public ResponseEntity<BrandProfileDto> getMyProfile(Authentication authentication) {
        return ResponseEntity.ok(profileService.getMyBrandProfile(authentication));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard(Authentication authentication) {
        List<CampaignResponse> campaigns = campaignService.getMyCampaigns(authentication);
        int activeCampaigns = (int) campaigns.stream()
                .filter(c -> c.getStatus() == CampaignStatus.OPEN || c.getStatus() == CampaignStatus.IN_PROGRESS)
                .count();
        BigDecimal totalBudget = campaigns.stream()
                .map(CampaignResponse::getBudget)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int pendingBids = 0;
        int acceptedBids = 0;
        Set<Long> influencerIds = new HashSet<>();
        for (CampaignResponse campaign : campaigns) {
            for (Bid bid : bidRepository.findByCampaignId(campaign.getId())) {
                if (bid.getStatus() == BidStatus.PENDING) pendingBids++;
                if (bid.getStatus() == BidStatus.ACCEPTED) acceptedBids++;
                influencerIds.add(bid.getInfluencerProfile().getId());
            }
        }

        return ResponseEntity.ok(Map.of(
                "activeCampaigns", activeCampaigns,
                "pendingBids", pendingBids,
                "acceptedBids", acceptedBids,
                "totalBudget", totalBudget,
                "totalInfluencers", influencerIds.size(),
                "recentActivity", List.of()
        ));
    }
}
