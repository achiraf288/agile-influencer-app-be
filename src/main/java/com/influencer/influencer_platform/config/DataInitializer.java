package com.influencer.influencer_platform.config;

import com.influencer.influencer_platform.entity.InfluencerProfile;
import com.influencer.influencer_platform.entity.User;
import com.influencer.influencer_platform.enums.UserRole;
import com.influencer.influencer_platform.repository.InfluencerProfileRepository;
import com.influencer.influencer_platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final InfluencerProfileRepository influencerProfileRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        String email = "User123@gmail.com";
        String password = "123456";
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            user = User.builder()
                    .email(email)
                    .passwordHash(passwordEncoder.encode(password))
                    .role(UserRole.INFLUENCER)
                    .fullName("Default Influencer")
                    .phone("")
                    .build();

            user = userRepository.save(user);

            influencerProfileRepository.save(InfluencerProfile.builder()
                    .user(user)
                    .niche("General")
                    .location("Unknown")
                    .build());
        } else {
            user.setPasswordHash(passwordEncoder.encode(password));
            if (user.getRole() == null) {
                user.setRole(UserRole.INFLUENCER);
            }
            if (user.getFullName() == null || user.getFullName().isBlank()) {
                user.setFullName("Default Influencer");
            }
            userRepository.save(user);

            if (influencerProfileRepository.findByUserId(user.getId()).isEmpty()) {
                influencerProfileRepository.save(InfluencerProfile.builder()
                        .user(user)
                        .niche("General")
                        .location("Unknown")
                        .build());
            }
        }
    }
}
