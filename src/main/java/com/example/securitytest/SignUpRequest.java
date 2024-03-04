package com.example.securitytest;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;


public record SignUpRequest(
        @NotNull(message = "이메일을 입력해 주세요.")
        String email,

        @NotNull(message = "비밀번호를 입력해 주세요")
        String password
) {

        @Builder
        public SignUpRequest{

        }
}
