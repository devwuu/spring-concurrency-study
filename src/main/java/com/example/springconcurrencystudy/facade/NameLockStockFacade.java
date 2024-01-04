package com.example.springconcurrencystudy.facade;

import com.example.springconcurrencystudy.repository.LockRepository;
import com.example.springconcurrencystudy.repository.StockRepository;
import com.example.springconcurrencystudy.service.StockService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class NameLockStockFacade {
    private final LockRepository lockRepository;
    private final StockService service;

    public NameLockStockFacade(LockRepository lockRepository, StockService service) {
        this.lockRepository = lockRepository;
        this.service = service;
    }

    @Transactional
    public void decrease(Long id, Long quantity){
        try {
            lockRepository.getLock(id.toString()); // stock id를 키로 하는 lock을 획득, 이렇게 하면 같은 id를 가진 lock을 다른 스레드에선 획득할 수 없기 때문에 대기 상태가 됨
            service.decreaseWithNamedLock(id, quantity); // lock 획득 후 비즈니스 로직 수행
        }finally {
            lockRepository.releaseLock(id.toString()); // stock id를 키로 하는 lock을 릴리즈 함. 해당 stock id를 키로 하는 lock을 다른 스레드가 획득할 수 있기 때문에 재고 감소가 다시 가능해짐
        }
    }


}
