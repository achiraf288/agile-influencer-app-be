package com.influencer.influencer_platform.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "social_media_links")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialMediaLink {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "influencer_profile_id", nullable = false)
    @JsonIgnore
    private InfluencerProfile influencerProfile;
    
    @Column(nullable = false)
    private String platform;
    
    @Column(nullable = false)
    private String url;
    
    private Long followerCount;
}
