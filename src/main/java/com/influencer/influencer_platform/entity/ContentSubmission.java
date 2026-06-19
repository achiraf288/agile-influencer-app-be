package com.influencer.influencer_platform.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.influencer.influencer_platform.enums.BidStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "content_submissions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentSubmission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", unique = true, nullable = false)
    @JsonIgnore
    private CampaignAssignment assignment;
    
    @Column(nullable = false)
    private String contentUrl;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BidStatus status = BidStatus.PENDING;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime submittedAt;
    
    @Column(columnDefinition = "TEXT")
    private String brandFeedback;
}
