package com.influencer.influencer_platform.service;

import com.influencer.influencer_platform.dto.response.BrandDashboardDto;
import com.influencer.influencer_platform.dto.response.BrandProfileDto;
import com.influencer.influencer_platform.dto.response.BidResponse;
import com.influencer.influencer_platform.dto.response.CampaignResponse;
import com.influencer.influencer_platform.dto.response.RecentActivityDto;
import com.influencer.influencer_platform.entity.BrandProfile;
import com.influencer.influencer_platform.entity.Campaign;
import com.influencer.influencer_platform.enums.BidStatus;
import com.influencer.influencer_platform.enums.CampaignStatus;
import com.influencer.influencer_platform.exception.ResourceNotFoundException;
import com.influencer.influencer_platform.repository.BrandProfileRepository;
import com.influencer.influencer_platform.repository.BidRepository;
import com.influencer.influencer_platform.repository.CampaignRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandProfileRepository brandProfileRepository;
    private final CampaignRepository campaignRepository;
    private final BidRepository bidRepository;
    private final ProfileService profileService;

    @Transactional(readOnly = true)
    public List<CampaignResponse> getBrandCampaigns(Authentication authentication) {
        Long userId = ((com.influencer.influencer_platform.security.UserPrincipal) authentication.getPrincipal()).getId();
        
        BrandProfile brandProfile = brandProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Brand profile not found"));

        return campaignRepository.findByBrandProfileIdWithBrandProfile(brandProfile.getId()).stream()
                .map(this::mapToCampaignResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BrandDashboardDto getBrandDashboard(Authentication authentication) {
        Long userId = ((com.influencer.influencer_platform.security.UserPrincipal) authentication.getPrincipal()).getId();
        
        BrandProfile brandProfile = brandProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Brand profile not found"));

        List<Campaign> allCampaigns = campaignRepository.findByBrandProfileIdWithBrandProfile(brandProfile.getId());
        long activeCampaigns = allCampaigns.stream()
                .filter(c -> c.getStatus() == CampaignStatus.ACTIVE)
                .count();

        List<com.influencer.influencer_platform.entity.Bid> allBids = allCampaigns.stream()
                .flatMap(c -> bidRepository.findByCampaignId(c.getId()).stream())
                .collect(Collectors.toList());

        long pendingBids = allBids.stream()
                .filter(b -> b.getStatus() == BidStatus.PENDING)
                .count();
        long acceptedBids = allBids.stream()
                .filter(b -> b.getStatus() == BidStatus.ACCEPTED)
                .count();

        Double totalBudget = allCampaigns.stream()
                .map(Campaign::getBudget)
                .reduce(0.0, Double::sum);

        long totalInfluencers = allBids.stream()
                .map(b -> b.getInfluencer().getId())
                .distinct()
                .count();

        // Build recent activity
        List<RecentActivityDto> recentActivity = buildRecentActivity(allCampaigns, allBids);

        return BrandDashboardDto.builder()
                .totalCampaigns((long) allCampaigns.size())
                .activeCampaigns(activeCampaigns)
                .pendingBids(pendingBids)
                .acceptedBids(acceptedBids)
                .totalBudget(totalBudget)
                .totalInfluencers(totalInfluencers)
                .recentActivity(recentActivity)
                .build();
    }

    public BrandProfileDto getBrandProfile(Authentication authentication) {
        Long userId = ((com.influencer.influencer_platform.security.UserPrincipal) authentication.getPrincipal()).getId();
        
        brandProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Brand profile not found"));

        return profileService.getBrandProfile(userId, authentication);
    }

    public BrandProfileDto updateBrandProfile(BrandProfileDto request, Authentication authentication) {
        return profileService.updateBrandProfile(request, authentication);
    }

    public BrandProfileDto updateBrandProfile(BrandProfileDto request, MultipartFile logo, Authentication authentication) {
        return profileService.updateBrandProfile(request, logo, authentication);
    }

    @Transactional(readOnly = true)
    public List<BidResponse> getBrandBids(Authentication authentication) {
        Long userId = ((com.influencer.influencer_platform.security.UserPrincipal) authentication.getPrincipal()).getId();
        
        BrandProfile brandProfile = brandProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Brand profile not found"));

        List<Campaign> campaigns = campaignRepository.findByBrandProfileIdWithBrandProfile(brandProfile.getId());
        
        return campaigns.stream()
                .flatMap(c -> bidRepository.findByCampaignId(c.getId()).stream())
                .map(this::mapToBidResponse)
                .collect(Collectors.toList());
    }

    private CampaignResponse mapToCampaignResponse(Campaign campaign) {
        return CampaignResponse.builder()
                .id(campaign.getId())
                .title(campaign.getTitle())
                .description(campaign.getDescription())
                .category(campaign.getCategory())
                .location(campaign.getLocation())
                .budget(campaign.getBudget())
                .deadline(campaign.getDeadline())
                .status(campaign.getStatus())
                .createdAt(campaign.getCreatedAt())
                .brandProfileId(campaign.getBrandProfile().getId())
                .companyName(campaign.getBrandProfile().getCompanyName())
                .build();
    }

    private BidResponse mapToBidResponse(com.influencer.influencer_platform.entity.Bid bid) {
        return BidResponse.builder()
                .id(bid.getId())
                .message(bid.getMessage())
                .proposedBudget(bid.getProposedBudget())
                .status(bid.getStatus())
                .createdAt(bid.getCreatedAt())
                .influencer(com.influencer.influencer_platform.dto.response.InfluencerSummaryDto.builder()
                        .id(bid.getInfluencer().getId())
                        .fullName(bid.getInfluencer().getFullName())
                        .niche(null) // TODO: Get from influencer profile
                        .followerCount(null) // TODO: Get from influencer profile
                        .engagementRate(null) // TODO: Get from influencer profile
                        .profilePicUrl(null) // TODO: Get from influencer profile
                        .build())
                .campaign(com.influencer.influencer_platform.dto.response.CampaignSummaryDto.builder()
                        .id(bid.getCampaign().getId())
                        .title(bid.getCampaign().getTitle())
                        .category(bid.getCampaign().getCategory())
                        .location(bid.getCampaign().getLocation())
                        .build())
                .build();
    }

    private List<RecentActivityDto> buildRecentActivity(List<Campaign> campaigns, List<com.influencer.influencer_platform.entity.Bid> bids) {
        List<RecentActivityDto> activities = new java.util.ArrayList<>();

        // Add campaign activities
        for (Campaign campaign : campaigns) {
            activities.add(RecentActivityDto.builder()
                    .id(campaign.getId())
                    .type("campaign")
                    .description("Created campaign: " + campaign.getTitle())
                    .createdAt(campaign.getCreatedAt())
                    .status(campaign.getStatus().name())
                    .build());
        }

        // Add bid activities
        for (com.influencer.influencer_platform.entity.Bid bid : bids) {
            activities.add(RecentActivityDto.builder()
                    .id(bid.getId())
                    .type("bid")
                    .description("New bid from " + bid.getInfluencer().getFullName() + " for " + bid.getCampaign().getTitle())
                    .createdAt(bid.getCreatedAt())
                    .status(bid.getStatus().name())
                    .build());
        }

        // Sort by most recent and limit to 10
        return activities.stream()
                .sorted(Comparator.comparing(RecentActivityDto::getCreatedAt).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }
}
