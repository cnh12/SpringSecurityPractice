package com.example.securitytest;

import lombok.Builder;

public record UpdateAccessTokenRequest(
        String refreshToken
) {

    @Builder
    public UpdateAccessTokenRequest {
    }
}
