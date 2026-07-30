package com.influencer.influencer_platform.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterInfluencerStep3Request {

    private Long userId;

    @Valid
    @NotNull(message = "Platforms are required")
    private List<SocialMediaPlatformRequest> platforms;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SocialMediaPlatformRequest {
        private String platform;
        private String url;
        private Long followerCount;
    }
}