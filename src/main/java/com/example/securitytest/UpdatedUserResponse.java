package com.example.securitytest;

import lombok.Builder;

public record UpdatedUserResponse(
        String email
) {

    @Builder
    public UpdatedUserResponse {

    }
}
