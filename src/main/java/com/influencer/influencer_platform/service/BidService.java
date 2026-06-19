package com.influencer.influencer_platform.service;

import com.influencer.influencer_platform.dto.request.BidRequest;
import com.influencer.influencer_platform.dto.response.BidResponse;
import com.influencer.influencer_platform.dto.response.CampaignSummaryDto;
import com.influencer.influencer_platform.dto.response.InfluencerSummaryDto;
import com.influencer.influencer_platform.entity.*;
import com.influencer.influencer_platform.enums.*;
import com.influencer.influencer_platform.exception.DuplicateBidException;
import com.influencer.influencer_platform.exception.ResourceNotFoundException;
import com.influencer.influencer_platform.exception.UnauthorizedException;
import com.influencer.influencer_platform.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BidService {

    private final BidRepository bidRepository;
    private final CampaignRepository campaignRepository;
    private final InfluencerProfileRepository influencerProfileRepository;
    private final CampaignAssignmentRepository campaignAssignmentRepository;
    private final NotificationService notificationService;

    @Transactional
    public BidResponse createBid(BidRequest request, Authentication authentication) {
        Long userId = ((com.influencer.influencer_platform.security.UserPrincipal) authentication.getPrincipal()).getId();
        
        InfluencerProfile influencerProfile = influencerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Influencer profile not found"));

        Campaign campaign = campaignRepository.findById(request.getCampaignId())
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + request.getCampaignId()));

        if (campaign.getStatus() != CampaignStatus.OPEN) {
            throw new UnauthorizedException("Campaign is not open for bidding");
        }

        if (bidRepository.findByCampaignIdAndInfluencerProfileId(request.getCampaignId(), influencerProfile.getId()).isPresent()) {
            throw new DuplicateBidException("You have already bid on this campaign");
        }

        Bid bid = Bid.builder()
                .campaign(campaign)
                .influencerProfile(influencerProfile)
                .proposal(request.getProposal())
                .proposedAmount(request.getProposedAmount())
                .status(BidStatus.PENDING)
                .build();

        bid = bidRepository.save(bid);

        notificationService.notifyBidReceived(
                campaign.getBrandProfile().getUser().getId(),
                influencerProfile.getUser().getFullName(),
                campaign.getTitle()
        );

        return mapToResponse(bid);
    }

    public List<BidResponse> getBidsByCampaign(Long campaignId, Authentication authentication) {
        Long userId = ((com.influencer.influencer_platform.security.UserPrincipal) authentication.getPrincipal()).getId();
        
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + campaignId));

        if (!campaign.getBrandProfile().getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to view bids for this campaign");
        }

        return bidRepository.findByCampaignId(campaignId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<BidResponse> getMyBids(Authentication authentication) {
        Long userId = ((com.influencer.influencer_platform.security.UserPrincipal) authentication.getPrincipal()).getId();
        
        InfluencerProfile influencerProfile = influencerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Influencer profile not found"));

        return bidRepository.findByInfluencerProfileId(influencerProfile.getId()).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public BidResponse acceptBid(Long bidId, Authentication authentication) {
        Long userId = ((com.influencer.influencer_platform.security.UserPrincipal) authentication.getPrincipal()).getId();
        
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new ResourceNotFoundException("Bid not found with id: " + bidId));

        if (!bid.getCampaign().getBrandProfile().getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to accept this bid");
        }

        if (bid.getStatus() != BidStatus.PENDING) {
            throw new UnauthorizedException("Bid is not in pending status");
        }

        bid.setStatus(BidStatus.ACCEPTED);
        bid = bidRepository.save(bid);

        Campaign campaign = bid.getCampaign();
        campaign.setStatus(CampaignStatus.IN_PROGRESS);
        campaignRepository.save(campaign);

        CampaignAssignment assignment = CampaignAssignment.builder()
                .bid(bid)
                .campaign(campaign)
                .influencerProfile(bid.getInfluencerProfile())
                .status(AssignmentStatus.ASSIGNED)
                .build();
        campaignAssignmentRepository.save(assignment);

        notificationService.notifyBidAccepted(
                bid.getInfluencerProfile().getUser().getId(),
                campaign.getTitle()
        );

        return mapToResponse(bid);
    }

    @Transactional
    public BidResponse rejectBid(Long bidId, Authentication authentication) {
        Long userId = ((com.influencer.influencer_platform.security.UserPrincipal) authentication.getPrincipal()).getId();
        
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new ResourceNotFoundException("Bid not found with id: " + bidId));

        if (!bid.getCampaign().getBrandProfile().getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to reject this bid");
        }

        bid.setStatus(BidStatus.REJECTED);
        bid = bidRepository.save(bid);

        notificationService.notifyBidRejected(
                bid.getInfluencerProfile().getUser().getId(),
                bid.getCampaign().getTitle()
        );

        return mapToResponse(bid);
    }

    private BidResponse mapToResponse(Bid bid) {
        InfluencerSummaryDto influencer = InfluencerSummaryDto.builder()
                .id(bid.getInfluencerProfile().getId())
                .fullName(bid.getInfluencerProfile().getUser().getFullName())
                .niche(bid.getInfluencerProfile().getNiche())
                .followerCount(bid.getInfluencerProfile().getFollowerCount())
                .engagementRate(bid.getInfluencerProfile().getEngagementRate())
                .profilePicUrl(bid.getInfluencerProfile().getProfilePicUrl())
                .build();

        CampaignSummaryDto campaign = CampaignSummaryDto.builder()
                .id(bid.getCampaign().getId())
                .title(bid.getCampaign().getTitle())
                .category(bid.getCampaign().getCategory())
                .location(bid.getCampaign().getLocation())
                .build();

        return BidResponse.builder()
                .id(bid.getId())
                .proposal(bid.getProposal())
                .proposedAmount(bid.getProposedAmount())
                .status(bid.getStatus())
                .submittedAt(bid.getSubmittedAt())
                .influencer(influencer)
                .campaign(campaign)
                .build();
    }
}
