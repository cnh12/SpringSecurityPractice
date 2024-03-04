package com.example.securitytest;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisRepository {

    private final RedisTemplate<Object, Object> redisTemplate;
    private ValueOperations<Object, Object> valueOperations;

    @PostConstruct
    public void init() {
        valueOperations = redisTemplate.opsForValue();
    }

    public void saveRefreshToken(String email, String token, int timeLimit){

//        valueOperations.set(email, token);
//        log.info("Redis test : {}", valueOperations.get(email));

        //refreshtoken을 기준으로 찾기 위해서 email이 아닌 token을 key로 설정
        valueOperations.set(token, email);
        saveKeyValue(token, email, timeLimit, TimeUnit.MILLISECONDS);
        log.info("Redis test : {}", valueOperations.get(token));

    }

    public String findEmailByRefreshToken(String token){
        ValueOperations<Object, Object> valueOperations = redisTemplate.opsForValue();

        if(valueOperations.get(token) == null) return null;
        return valueOperations.get(token).toString();

    }

    public void logout(User user) {
        //Redis에서 토큰 삭제
        //지금은 key가 token이라 찾기 어려움
    }

    private void saveKeyValue(String key, String value, int limitMinute, TimeUnit timeUnit){
        try{ // 미봉책. 나중에 더 상세히 파 볼 것.
            valueOperations.set(key, value, limitMinute, timeUnit);
            log.info("key: {}, value: {} 로 {} 간 redis 저장", key, value, limitMinute);
        }catch(NullPointerException ignored){}

    }
}
