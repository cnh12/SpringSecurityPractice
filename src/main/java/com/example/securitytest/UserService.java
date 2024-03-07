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
    private final UserRepository userRepository;


    public User signup(SignUpRequest request){


        userRepository.findByEmail(request.email()).ifPresent(
                (user) -> {throw new RuntimeException("이미 가입된 이메일입니다.");}
        );

        User user = User.builder()
                .email(request.email()).password(request.password())
                .roles(Set.of(Role.USER))
                .build();
        return userRepository.save(user);
    }


    public User login(LoginUserRequest request){
        log.info("login 진입 {}",request);

        //DB에서 id, pw 확인
        Optional<User> foundUser = userRepository.findByEmail(request.email());
        if(foundUser.isPresent()){
            User user = foundUser.get();
            if(request.password().equals(foundUser.get().getPassword())){
                return user;
            }
        }
        
        throw new RuntimeException("아이디가 존재하지 않거나 아이디나 비밀번호가 일치하지 않습니다.");

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
//        String email = redisRepository.findEmailByRefreshToken(refreshToken);
//        if(email==null) return null;
//        return email;
        return "";

    }

    public User findUserByEmail(String email) {

        //실제 DB에서 User 찾아서 갖고옴.
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("email에 해당하는 user가 없습니다."));

    }

    /*
    public void logout(User user) {
        redisRepository.logout(user);
    }

     */
}
