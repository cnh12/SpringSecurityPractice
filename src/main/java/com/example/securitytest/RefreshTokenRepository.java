package com.example.securitytest;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

    //트러블슈팅 - 처음에는 다른 곳에서 refreshTokenRepository. 하면 다른 메소드들이 호출이 안됐다가 밑에 메소드 주석처리하니까 정상호출
    //그런데 다시 해보니 주석처리 안해도 오류X. 그냥 재실행하면 되는건가...? 인텔리제이 이상;;
    Optional<RefreshToken> findByEmail(String email);

    Optional<RefreshToken> findByRefreshToken(String refreshToken);
}
