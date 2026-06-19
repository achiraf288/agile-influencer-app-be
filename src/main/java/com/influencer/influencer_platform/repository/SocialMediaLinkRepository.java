package com.influencer.influencer_platform.repository;

import com.influencer.influencer_platform.entity.SocialMediaLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SocialMediaLinkRepository extends JpaRepository<SocialMediaLink, Long> {
    List<SocialMediaLink> findByInfluencerProfileId(Long influencerProfileId);
}
