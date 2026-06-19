package com.influencer.influencer_platform.service;

import com.influencer.influencer_platform.dto.request.CampaignRequest;
import com.influencer.influencer_platform.dto.response.CampaignResponse;
import com.influencer.influencer_platform.entity.BrandProfile;
import com.influencer.influencer_platform.entity.Campaign;
import com.influencer.influencer_platform.enums.CampaignStatus;
import com.influencer.influencer_platform.exception.ResourceNotFoundException;
import com.influencer.influencer_platform.exception.UnauthorizedException;
import com.influencer.influencer_platform.repository.BrandProfileRepository;
import com.influencer.influencer_platform.repository.CampaignRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final BrandProfileRepository brandProfileRepository;

    @Transactional
    public CampaignResponse createCampaign(CampaignRequest request, Authentication authentication) {
        Long userId = ((com.influencer.influencer_platform.security.UserPrincipal) authentication.getPrincipal()).getId();
        
        BrandProfile brandProfile = brandProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Brand profile not found"));

        Campaign campaign = Campaign.builder()
                .brandProfile(brandProfile)
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .location(request.getLocation())
                .budget(request.getBudget())
                .deadline(request.getDeadline())
                .status(CampaignStatus.OPEN)
                .build();

        campaign = campaignRepository.save(campaign);

        return mapToResponse(campaign);
    }

    public List<CampaignResponse> getOpenCampaigns(String category, String location) {
        Specification<Campaign> spec = (root, query, cb) -> cb.equal(root.get("status"), CampaignStatus.OPEN);
        
        if (category != null && !category.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category"), category));
        }
        
        if (location != null && !location.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("location"), location));
        }

        return campaignRepository.findAll(spec).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<CampaignResponse> getMyCampaigns(Authentication authentication) {
        Long userId = ((com.influencer.influencer_platform.security.UserPrincipal) authentication.getPrincipal()).getId();
        
        BrandProfile brandProfile = brandProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Brand profile not found"));

        return campaignRepository.findByBrandProfileId(brandProfile.getId()).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public CampaignResponse getCampaignById(Long id) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + id));
        return mapToResponse(campaign);
    }

    @Transactional
    public CampaignResponse updateCampaignStatus(Long id, CampaignStatus status, Authentication authentication) {
        Long userId = ((com.influencer.influencer_platform.security.UserPrincipal) authentication.getPrincipal()).getId();
        
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + id));

        if (!campaign.getBrandProfile().getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to update this campaign");
        }

        campaign.setStatus(status);
        campaign = campaignRepository.save(campaign);

        return mapToResponse(campaign);
    }

    private CampaignResponse mapToResponse(Campaign campaign) {
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
}
