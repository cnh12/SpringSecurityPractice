package com.example.securitytest;

import lombok.Builder;

public record JwtTokenResponse(String accessToken, String refreshToken, String tokenType) {
    @Builder
    public JwtTokenResponse{

    }
}
