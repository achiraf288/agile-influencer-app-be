package com.influencer.influencer_platform.service;

import com.influencer.influencer_platform.dto.request.LoginRequest;
import com.influencer.influencer_platform.dto.request.RegisterRequest;
import com.influencer.influencer_platform.dto.response.AuthResponse;
import com.influencer.influencer_platform.entity.BrandProfile;
import com.influencer.influencer_platform.entity.InfluencerProfile;
import com.influencer.influencer_platform.entity.User;
import com.influencer.influencer_platform.enums.UserRole;
import com.influencer.influencer_platform.exception.DuplicateBidException;
import com.influencer.influencer_platform.repository.BrandProfileRepository;
import com.influencer.influencer_platform.repository.InfluencerProfileRepository;
import com.influencer.influencer_platform.repository.UserRepository;
import com.influencer.influencer_platform.security.JwtTokenProvider;
import com.influencer.influencer_platform.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final BrandProfileRepository brandProfileRepository;
    private final InfluencerProfileRepository influencerProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateBidException("Email already registered");
        }

        UserRole role = UserRole.valueOf(request.getRole().toUpperCase());

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .build();

        user = userRepository.save(user);

        if (role == UserRole.BRAND) {
            BrandProfile brandProfile = BrandProfile.builder()
                    .user(user)
                    .companyName(request.getCompanyName())
                    .build();
            brandProfileRepository.save(brandProfile);
        } else {
            InfluencerProfile influencerProfile = InfluencerProfile.builder()
                    .user(user)
                    .niche(request.getNiche())
                    .location(request.getLocation())
                    .build();
            influencerProfileRepository.save(influencerProfile);
        }

        String token = tokenProvider.generateToken(
                new UsernamePasswordAuthenticationToken(
                        UserPrincipal.create(user),
                        null,
                        UserPrincipal.create(user).getAuthorities()
                )
        );

        return AuthResponse.builder()
                .token(token)
                .role(role.name())
                .email(user.getEmail())
                .userId(user.getId())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        String token = tokenProvider.generateToken(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        return AuthResponse.builder()
                .token(token)
                .role(userPrincipal.getRole().name())
                .email(userPrincipal.getEmail())
                .userId(userPrincipal.getId())
                .build();
    }
}
