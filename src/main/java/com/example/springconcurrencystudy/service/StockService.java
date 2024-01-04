package com.example.springconcurrencystudy.service;

import com.example.springconcurrencystudy.domain.Stock;
import com.example.springconcurrencystudy.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class StockService {

    private final StockRepository repository;

    public StockService(StockRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void decrease(Long id, Long quantity){
        Stock stock = repository.findById(id).orElseThrow();
        stock.decrease(quantity);
        repository.saveAndFlush(stock);
    }

    // synchronized 키워드를 이용하면 한 스레드만이 하나의 공유자원에 접근하도록 합니다.
    // 여기선 thread가 순차적으로 decrease 메서드를 실행하게 합니다
//    @Transactional
    public synchronized void decreaseSync(Long id, Long quantity){
        Stock stock = repository.findById(id).orElseThrow();
        stock.decrease(quantity);
        repository.saveAndFlush(stock);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW) // 상위의 transaction과 별개로 수행되어야 하기 때문에 프로퍼게이션을 변경해준다
    public void decreaseWithNamedLock(Long id, Long quantity){
        Stock stock = repository.findById(id).orElseThrow();
        stock.decrease(quantity);
        repository.saveAndFlush(stock);
    }






}
