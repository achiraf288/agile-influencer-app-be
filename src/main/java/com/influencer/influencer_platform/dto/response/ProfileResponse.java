package com.influencer.influencer_platform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {
    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private String role;
    
    private BrandProfileDto brandProfile;
    private InfluencerProfileDto influencerProfile;
}
