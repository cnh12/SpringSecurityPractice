package com.example.securitytest;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String token = jwtTokenProvider.resolveToken(request);
        JwtCode code = jwtTokenProvider.validateToken(token);
        log.info("doFilterInternal 진입 : {}",code);
        if(code == JwtCode.ACCESS){
            log.info("doFilterInternal access");
            Authentication authentication = jwtTokenProvider.getAuthentication(token);
            log.info("doFilterInternal authentication : {}",authentication);
            log.info("doFilterInternal authentication.getAuthorities : {}", authentication.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

        }
        chain.doFilter(request, response);

    }
}
