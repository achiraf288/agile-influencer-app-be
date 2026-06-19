package com.influencer.influencer_platform.repository;

import com.influencer.influencer_platform.entity.InfluencerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InfluencerProfileRepository extends JpaRepository<InfluencerProfile, Long> {
    Optional<InfluencerProfile> findByUserId(Long userId);
}
