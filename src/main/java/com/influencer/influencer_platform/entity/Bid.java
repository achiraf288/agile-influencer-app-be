package com.influencer.influencer_platform.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.influencer.influencer_platform.enums.BidStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bids", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"campaign_id", "influencer_profile_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Bid {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    @JsonIgnore
    private Campaign campaign;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "influencer_profile_id", nullable = false)
    @JsonIgnore
    private InfluencerProfile influencerProfile;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String proposal;
    
    @Column(nullable = false)
    private BigDecimal proposedAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BidStatus status = BidStatus.PENDING;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime submittedAt;
}
