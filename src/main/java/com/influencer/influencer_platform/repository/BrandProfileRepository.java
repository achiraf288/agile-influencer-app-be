package com.influencer.influencer_platform.repository;

import com.influencer.influencer_platform.entity.BrandProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BrandProfileRepository extends JpaRepository<BrandProfile, Long> {
    Optional<BrandProfile> findByUserId(Long userId);
    
    @Query("SELECT bp FROM BrandProfile bp LEFT JOIN FETCH bp.user WHERE bp.user.id = :userId")
    Optional<BrandProfile> findByUserIdWithUser(@Param("userId") Long userId);
}
