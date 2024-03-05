package com.example.securitytest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        //DB 사용하지 않았을 때 임시방편
//        log.info("loadUserByUsername : {}",username);
//        HashSet hs = new HashSet<Role>();
//        hs.add(Role.USER);
//        return new User(1L, username, "", hs);


        return userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("아이디나 비밀번호가 일치하지 않습니다."));
    }
}
