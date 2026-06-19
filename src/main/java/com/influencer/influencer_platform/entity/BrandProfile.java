package com.influencer.influencer_platform.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "brand_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandProfile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    @JsonIgnore
    private User user;
    
    @Column(nullable = false)
    private String companyName;
    
    private String industry;
    
    private String website;
    
    private String bio;
    
    private String logoUrl;
}
