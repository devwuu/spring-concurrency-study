package com.example.springconcurrencystudy.repository;

import org.springframework.context.annotation.Conditional;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RedisLockRepository{

    // redis를 사용하게 되면 db의 named lock을 직접 사용할 때보다
    // session 관리가 용이하다

    private RedisTemplate<String, String> redisTemplate;

    public RedisLockRepository(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Boolean lock(Long key){
        return redisTemplate
                .opsForValue()
                .setIfAbsent(generateKey(key), "lock", Duration.ofMillis(3_000));
        // redis 에 key를 key로 하여 lock이라는 value를 저장한다
        // 3000ms 동안 실패하면 time out 된다
        // 값이 정상적으로 저장된다면 true ( lock 획득 성공)
        // 값이 정상적으로 저장되지 않는다면 false ( lock 획득 실패)
    }

    // redis에서 해당 key를 삭제한다 => lock 반환
    public Boolean unlock(Long key){
        return redisTemplate.delete(generateKey(key));
    }

    private String generateKey(Long key) {
        return key.toString();
    }


}
