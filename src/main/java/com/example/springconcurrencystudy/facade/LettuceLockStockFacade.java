package com.example.springconcurrencystudy.facade;

import com.example.springconcurrencystudy.repository.RedisLockRepository;
import com.example.springconcurrencystudy.service.StockService;
import org.springframework.stereotype.Component;

@Component
public class LettuceLockStockFacade {

    private final RedisLockRepository redisLockRepository;
    private final StockService service;

    public LettuceLockStockFacade(RedisLockRepository redisLockRepository, StockService service) {
        this.redisLockRepository = redisLockRepository;
        this.service = service;
    }

    public void decrease(Long id, Long quantity) throws InterruptedException {
        while (!redisLockRepository.lock(id)){
            Thread.sleep(100); // lock 획득에 실패하면 100ms 후 재시도를 한다
        }

        // 락 획득에 성공한다면
        try {
            service.decreaseWithNamedLock(id, quantity); // 재고 감소
        }finally {
            redisLockRepository.unlock(id); // 락반환
        }

    }

    // lettuce
    // spin lock 방식.
    // 스레드가 락을 획득하려는 시도를 반복해서 수행함
    // 이 반복 수행 로직을 개발자가 작성해줘야 함
    // setnx 명령어를 활용해서 분산락 구현
    // setnx? 락 획득을 시도(set) => 존재하면 획득 실패( 획득할 때까지 재시도), 존재하지 않으면 획득

    // 구현이 간단하다는 장점이 있지만 redis에 부하를 줄 수 있다.
    // 따라서 thread sleep처럼 redis의 부하를 줄일 수 있는 방법이 필요하다




}
