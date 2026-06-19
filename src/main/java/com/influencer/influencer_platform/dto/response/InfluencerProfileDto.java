package com.influencer.influencer_platform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InfluencerProfileDto {
    private Long id;
    private String niche;
    private String location;
    private Long followerCount;
    private Double engagementRate;
    private String profilePicUrl;
    private String bio;
    private List<SocialMediaLinkDto> socialMediaLinks;
}
