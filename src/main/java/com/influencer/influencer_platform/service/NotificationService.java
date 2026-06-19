package com.influencer.influencer_platform.service;

import com.influencer.influencer_platform.entity.Notification;
import com.influencer.influencer_platform.entity.User;
import com.influencer.influencer_platform.repository.NotificationRepository;
import com.influencer.influencer_platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public void notifyBidReceived(Long brandUserId, String influencerName, String campaignTitle) {
        User user = userRepository.findById(brandUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Notification notification = Notification.builder()
                .user(user)
                .type("BID_RECEIVED")
                .message(String.format("New bid received from %s for campaign: %s", influencerName, campaignTitle))
                .build();

        notificationRepository.save(notification);
        // TODO: emit via WebSocket/SSE
    }

    @Transactional
    public void notifyBidAccepted(Long influencerUserId, String campaignTitle) {
        User user = userRepository.findById(influencerUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Notification notification = Notification.builder()
                .user(user)
                .type("BID_ACCEPTED")
                .message(String.format("Your bid for campaign '%s' has been accepted!", campaignTitle))
                .build();

        notificationRepository.save(notification);
        // TODO: emit via WebSocket/SSE
    }

    @Transactional
    public void notifyBidRejected(Long influencerUserId, String campaignTitle) {
        User user = userRepository.findById(influencerUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Notification notification = Notification.builder()
                .user(user)
                .type("BID_REJECTED")
                .message(String.format("Your bid for campaign '%s' has been rejected.", campaignTitle))
                .build();

        notificationRepository.save(notification);
        // TODO: emit via WebSocket/SSE
    }

    @Transactional
    public void notifyContentSubmitted(Long brandUserId, String influencerName, String campaignTitle) {
        User user = userRepository.findById(brandUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Notification notification = Notification.builder()
                .user(user)
                .type("CONTENT_SUBMITTED")
                .message(String.format("Content submitted by %s for campaign: %s", influencerName, campaignTitle))
                .build();

        notificationRepository.save(notification);
        // TODO: emit via WebSocket/SSE
    }

    @Transactional
    public void notifyContentReviewed(Long influencerUserId, String status, String campaignTitle) {
        User user = userRepository.findById(influencerUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Notification notification = Notification.builder()
                .user(user)
                .type("CONTENT_REVIEWED")
                .message(String.format("Your content for campaign '%s' has been %s.", campaignTitle, status.toLowerCase()))
                .build();

        notificationRepository.save(notification);
        // TODO: emit via WebSocket/SSE
    }
}
