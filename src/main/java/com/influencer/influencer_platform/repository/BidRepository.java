package com.influencer.influencer_platform.repository;

import com.influencer.influencer_platform.entity.Bid;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BidRepository extends JpaRepository<Bid, Long> {
    List<Bid> findByCampaignId(Long campaignId);
    List<Bid> findByInfluencerProfileId(Long influencerProfileId);
    Optional<Bid> findByCampaignIdAndInfluencerProfileId(Long campaignId, Long influencerProfileId);
}
