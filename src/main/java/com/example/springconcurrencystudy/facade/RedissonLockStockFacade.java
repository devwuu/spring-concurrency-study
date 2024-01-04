package com.example.springconcurrencystudy.facade;

import com.example.springconcurrencystudy.service.StockService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class RedissonLockStockFacade {

    private final RedissonClient client; // redisson은 분산락 구현을 위한 repository를 제공해주기 때문에 repository부터 만들지 않아도 괜찮다
    private final StockService service;

    public RedissonLockStockFacade(RedissonClient client, StockService service) {
        this.client = client;
        this.service = service;
    }

    public void decrease(Long id, Long quantity){
        RLock lock = client.getLock(id.toString());
        try {
            boolean available = lock.tryLock(10, 1, TimeUnit.SECONDS);
            // 락 획득을 시도할 시간, 점유 시간, 시간 단위
            if(!available) { // 락 획득 실패시
                log.info("락 획득 실패");
                return;
            }
            service.decreaseWithNamedLock(id, quantity);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            lock.unlock();
        }
    }

    // redission
    // pub-sub 기반으로 락 구현
    // 스레드가 lock이 해제되었다는 메세지를 발생시키면
    // pub이 락이 해제되었음을 알려주고
    // 락을 획득하기 위해 대기 하고 있던 sub가 락을 획득한다.
    // 따라서 lettuce 처럼 반복해서 락 획득을 시도하는 것이 아니라 메세지가 전달된 타이밍에
    // 한 번, 혹은 수 번 내외로만 시도하기 때문에 redis의 부하를 줄여줄 수 있다.
    // 개발자가 별도의 retry 시도 로직을 작성하지 않아도 된다

    // 하지만 별도의 라이브러리(redisson)를 사용해야 하고 구현 방법이 복잡할 수 있다.

}
