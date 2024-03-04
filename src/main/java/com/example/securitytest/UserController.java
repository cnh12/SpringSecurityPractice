package com.example.securitytest;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
@RestController
@Slf4j
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    @DeleteMapping("/userlogout")
    public String logout(@AuthenticationPrincipal User user){
        log.info("logout 진입");
        userService.logout(user);
        return "로그아웃 완료 "+user.getEmail();
    }


    @PostMapping("/login")
    public JwtTokenResponse login(@RequestBody LoginUserRequest request, HttpServletResponse response){
        log.info("controller login 진입");
        User user = userService.login(request);

        return jwtTokenProvider.makeJwtTokenResponse(user);
    }

    @GetMapping("/test")
    public String test(@AuthenticationPrincipal User user){
        log.info("test완료 : {}",user);
        return "test완료 " + user.getEmail();
    }

    @PostMapping("/refreshtoken")
    public JwtTokenResponse updateAccessToken(@RequestBody UpdateAccessTokenRequest request){
        String email = userService.findEmailByRefreshToken(request.refreshToken());
        log.info("refreshToken : {}", email);
        if(email==null){
            return jwtTokenProvider.makeJwtTokenResponseWithNull();
        }

        //Redis의 Timeout을 사용하지 않았다면 DB에서 email을 가져온 후,
        //이 token이 만료되었는지 여부도 따져야 함.

        User user = userService.findUserByEmail(email);
        String accessToken = jwtTokenProvider.makeAccessToken(user.getEmail(), user.getRoles());
        return jwtTokenProvider.makeJwtTokenResponseWithToken(accessToken, request.refreshToken());
    }



}
