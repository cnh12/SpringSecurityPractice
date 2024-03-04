package com.example.securitytest;

import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final RedisRepository redisRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public User login(LoginUserRequest request){
        log.info("login 진입 {}",request);

        User user = User.builder()
                .email(request.email())
                .password(request.password())
                .build();

        log.info("login 완료 {}",user);
        return user;

    }

    public String findEmailByRefreshToken(String refreshToken){
        //redis에 refreshToken있는지, refreshToken이 유효한지 검사
//        String ref = redisRepository.getRefreshToken("tstem");

        //이부분이 고민.
        //email을 얻기 위해 refreshToken과 getUserPrimaryKey를 이용하면 애초에 시간 지나면 오류가 남.
        //그렇다면 굳이 redis를 갈필요가 없어지는건가 ..? redis는 로그아웃만 상관이 있게 되는건가 ...?
        /*
        try{
            String ref = redisRepository.getRefreshToken(jwtTokenProvider.getUserPrimaryKey(refreshToken));
            if(ref==null) return null;
            return ref;
        }
        catch (Exception e){
            return null;
        }
        
         */
        
        //결국 (token, email)의 형식으로 redis에 저장하여 email을 찾음
        String email = redisRepository.findEmailByRefreshToken(refreshToken);
        if(email==null) return null;
        return email;

    }

    public User findUserByEmail(String email) {

        //원래는 실제로 DB에서 User를 찾아서 갖고와야함.
        HashSet hs = new HashSet<Role>();
        hs.add(Role.USER);

        return User.builder()
                .email(email)
                .password("TESTPW")
                .roles(hs)
                .build();
    }

    public void logout(User user) {
        redisRepository.logout(user);
    }
}
