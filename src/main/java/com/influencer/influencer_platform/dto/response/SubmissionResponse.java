package com.influencer.influencer_platform.dto.response;

import com.influencer.influencer_platform.enums.BidStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionResponse {
    private Long id;
    private String contentUrl;
    private String description;
    private BidStatus status;
    private LocalDateTime submittedAt;
    private String brandFeedback;
}
