package com.example.securitytest;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Set;

enum JwtCode {
    DENIED, ACCESS, EXPIRED
}
@Component
@Slf4j
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final UserDetailsService userDetailsService;
    private final RedisRepository redisRepository;


    @Value("${jwt.secret.key}")
    private String secretKey;

    public static int tokenValidTime = 60 * 1000; // 1분

    public static int refreshTokenValidTime = 2 * 60 * 1000; // 2분


    private String tokenType = "Bearer";


    public String resolveToken(HttpServletRequest request) {
        return request.getHeader("AUTH-TOKEN");
    }

    public JwtCode validateToken(String token) {
        if(token == null){
            return JwtCode.DENIED;
        }

        try{
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return JwtCode.ACCESS;
        }catch(ExpiredJwtException e){
            return JwtCode.EXPIRED;
        }catch(JwtException | IllegalArgumentException e){
            log.info("잘못된 JWT 서명입니다.");
        }

        return JwtCode.DENIED;
    }


    public Authentication getAuthentication(String token) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(this.getUserPrimaryKey(token));
        log.info("getAuthentication : {}",userDetails);
        log.info("getAuthentication : {}",userDetails.getUsername());

        //여기서 SecurityContextHolder.getContext().setAuthentication(authentication); 하는데는 UserDetail에서 role이 있어야 getAuthorities가 정상작동 되는것을 확인할 수 있었다.
        log.info("getAuthentication : {}",userDetails.getAuthorities());


        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public String getUserPrimaryKey(String token) throws ExpiredJwtException {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }


    public JwtTokenResponse makeJwtTokenResponse(User user) {
        String accessToken = makeAccessToken(user.getEmail(), user.getRoles());
        String refreshToken = makeRefreshToken(user.getEmail());

//        redisRepository.saveRefreshToken(user.getEmail(), refreshToken, refreshTokenValidTime);



        return JwtTokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType(tokenType)
                .build();
    }

    public JwtTokenResponse makeJwtTokenResponseWithToken(String accessToken, String refreshToken) {
        return JwtTokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType(tokenType)
                .build();
    }

    public String makeAccessToken(String email, Set<Role> roles) {
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("roles", roles);

        Date now = new Date();

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + tokenValidTime))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public String makeRefreshToken(String email){
        Claims claims = Jwts.claims().setSubject(email);
        Date now = new Date();

        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + refreshTokenValidTime))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();

        return token;
    }


    //이제 Redis를 사용하지 않으므로 DB에 없다고 해서 토큰이 만료된 것이 아님.
    /*
    public JwtTokenResponse makeJwtTokenResponseWithNull() {
        return JwtTokenResponse.builder()
                .accessToken("Logout Required")
                .refreshToken("Logout Required")
                .tokenType(tokenType)
                .build();
    }

     */
}
