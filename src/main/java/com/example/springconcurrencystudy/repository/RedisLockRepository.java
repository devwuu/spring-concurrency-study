package com.example.springconcurrencystudy.repository;

import org.springframework.context.annotation.Conditional;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RedisLockRepository{

    // redis를 사용했을 때 장단점
    // db의 named lock을 직접 사용할 때보다 session 관리가 용이하다
    // 활용중인 redis가 없다면 별도의 구축 비용과 관리 비용이 발생한다
    // mysql보다 성능이 좋다 => 더 많은 요청 처리가 가능하다

    // mysql을 사용했을 때 장단점
    // 이미 mysql을 사용하고 있다면 별도의 비용 없이 사용 가능하다
    // 어느 정도의 트래픽까지는 문제없이 활용 가능하다
    // redis보다는 성능이 좋지 않다

    // 따라서 실무에서는 많은 요청을 필요로 하거나 이미 활용중인 redis가 있을 때 redis를 사용한다.
    // 비용적 여유가 없거나 요청이 충분히 많지 않은 경우엔 mysql을 사용한다.

    // Lettuce 의 장단점
    // 구현이 간단하고 Spring data redis를 사용하면 Lettuce가 기본이기 때문에 별도의 라이브러리를 사용하지 않아도 된다
    // 하지만 spin lock 방식이기 때문에 동시에 많은 스레드가 lock을 획득하기 위해 대기중이라면 redis에 부하가 갈 수 있다 (반복해서 시도하기 때문)

    // Redisson의 장단점
    // 락 획득 재시도를 기본으로 제공한다.
    // pub-sub 방식으로 구현되어 있기 때문에 Lettuce에 비해 redis에 부하가 덜 된다
    // 하지만 별도의 라이브러리를 사용해야 하고 lock을 라이브러리 차원에서 제공해주기 때문에 사용법을 공부해야 한다.

    // 실무에서는 재시도가 필요하지 않는 lock 은 Lettuce를, 재시도가 필요한 lock은 redisson을 사용한다

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
