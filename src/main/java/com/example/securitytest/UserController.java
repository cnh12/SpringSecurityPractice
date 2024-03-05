package com.example.securitytest;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
@RestController
@Slf4j
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;


    @DeleteMapping("/userlogout")
    public String logout(@AuthenticationPrincipal User user){
        log.info("logout 진입");
        
        //DB에서 해당 유저의 email로 refreshToken 존재 여부 파악 후 존재하면 삭제
//        userService.logout(user);
        RefreshToken refreshTokenDto = refreshTokenRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new RuntimeException("해당 이메일로 로그인된 기록이 없습니다."));
        refreshTokenRepository.delete(refreshTokenDto);
        return "로그아웃 완료 "+user.getEmail();
    }

    @PostMapping("/signup")
    public String signup(@RequestBody SignUpRequest request){
        User user = userService.signup(request);
        return user.getEmail();
    }


    @PostMapping("/login")
    public JwtTokenResponse login(@RequestBody LoginUserRequest request, HttpServletResponse response){
        log.info("controller login 진입");
        User user = userService.login(request);

        JwtTokenResponse jwtTokenResponse = jwtTokenProvider.makeJwtTokenResponse(user);

        //DB에 refreshtoken 저장
        System.out.println(refreshTokenRepository);
        Optional<RefreshToken> currentRefreshTokenDto = refreshTokenRepository.findByEmail(user.getEmail());
        if(currentRefreshTokenDto.isPresent()){
            refreshTokenRepository.delete(currentRefreshTokenDto.get());
        }
        refreshTokenRepository.save(
                RefreshToken.builder()
                        .email(user.getEmail()).refreshToken(jwtTokenResponse.refreshToken())
                        .build());

        return jwtTokenResponse;
    }

    @Transactional //"failed to lazily initialize a collection of role" 오류 발생하여 추가
    @GetMapping("/test")
    public String test(@AuthenticationPrincipal User user){
        log.info("test완료 : {}",user);
        return "test완료 " + user.getEmail();
    }

    @PostMapping("/refreshtoken")
    public JwtTokenResponse updateAccessToken(@RequestBody UpdateAccessTokenRequest request){
        
        //DB에 해당 RefreshToken이 있는지 확인
//        String email = userService.findEmailByRefreshToken(request.refreshToken());
        Optional<RefreshToken> refreshTokenDto = refreshTokenRepository.findByRefreshToken(request.refreshToken());
        log.info("refreshToken : {}", refreshTokenDto);
        if(refreshTokenDto.isEmpty()) {
            throw new RuntimeException("해당 이메일로 로그인된 기록이 없습니다.");
        }

        //DB에 해당 RefreshToken이 있어도 만료여부를 검사해야 함.
        if (jwtTokenProvider.validateToken(refreshTokenDto.get().getRefreshToken()) != JwtCode.ACCESS) {
//            return jwtTokenProvider.makeJwtTokenResponseWithNull();
            refreshTokenRepository.delete(refreshTokenDto.get());
            throw new RuntimeException("refreshtoken이 남아있지만 만료되었습니다. 자동삭제합니다.");
        }

        User user = userService.findUserByEmail(refreshTokenDto.get().getEmail());
        String accessToken = jwtTokenProvider.makeAccessToken(user.getEmail(), user.getRoles());
        return jwtTokenProvider.makeJwtTokenResponseWithToken(accessToken, request.refreshToken());
    }

}
