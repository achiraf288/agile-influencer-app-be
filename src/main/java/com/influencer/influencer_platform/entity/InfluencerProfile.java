package com.influencer.influencer_platform.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "influencer_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InfluencerProfile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    @JsonIgnore
    private User user;
    
    @Column(nullable = false)
    private String niche;
    
    private String location;
    
    private Long followerCount;
    
    private Double engagementRate;
    
    private String profilePicUrl;
    
    private String bio;
    
    @OneToMany(mappedBy = "influencerProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<SocialMediaLink> socialMediaLinks;
}
