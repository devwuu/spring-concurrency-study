package com.example.springconcurrencystudy.service;

import com.example.springconcurrencystudy.domain.Stock;
import com.example.springconcurrencystudy.repository.StockRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class StockServiceTest {

    @Autowired
    private StockService service;

    @Autowired
    private StockRepository repository;

    @BeforeEach
    public void init(){
        repository.saveAndFlush(new Stock(1L, 100L));
    }

    @AfterEach
    public void after(){
        repository.deleteAll();
    }

    @Test
    @DisplayName("재고가 정상적으로 감소합니다")
    public void decrease() {
        //given
        //when
        service.decrease(1L, 20L);
        //then
        Stock stock = repository.findById(1L).orElseThrow();
        assertThat(stock.getQuantity()).isEqualTo(80L);
    }
    
    @Test @DisplayName("동시성 문제가 발생합니다")
    public void concurrencyTest1() throws InterruptedException {
        //given
        int requestCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(requestCount); // 멀티 스레드에서 모든 요청이 완료될 때까지 기다리게 한다
        //when
        for(int i = 0; i < requestCount; i++){
            executorService.submit(() -> {
                try {
                    service.decrease(1L, 1L);
                }finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        //then
        Stock stock = repository.findById(1L).orElseThrow();
        assertThat(stock.getQuantity()).isEqualTo(0L);
        //org.opentest4j.AssertionFailedError:
        //expected: 0L
        // but was: 89L
        // 여러개의 스레드가 한개의 공유자원(db data)에 접근하게 되면서
        // 미처 commit되기 전의 데이터를 읽어가서 update 해버리는 바람에
        // 감소 수량이 누락되게 된다.

    }


    @Test @DisplayName("sync 키워드를 이용해 동시성 문제를 해결해보려고 합니다")
    public void concurrencyTest2() throws InterruptedException {
        //given
        int requestCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(requestCount); // 멀티 스레드에서 모든 요청이 완료될 때까지 기다리게 한다
        //when
        for(int i = 0; i < requestCount; i++){
            executorService.submit(() -> {
                try {
                    service.decreaseSync(1L, 1L);
                }finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        //then
        Stock stock = repository.findById(1L).orElseThrow();
        assertThat(stock.getQuantity()).isEqualTo(0L);
        // org.opentest4j.AssertionFailedError:
        // expected: 0L
        // but was: 50L

        // @Transactional 을 통해 AOP가 적용될 때
        // proxy 객체로 변경되면서 문제가 발생하게 된다
        // transaction start => decrease => transaction end 순으로 로직이 실행되는데
        // 트랜잭션이 종료될 때 commit이 이루어지게 된다
        // 문제는 commit 되기 전에 select가 이루어지기 때문에
        // (service 입장에선 이미 로직이 끝나버렸기 때문에 다른 스레드에서 decrease 를 호출해버림 => select 실행)
        // (synchronized가 걸린 메서드는 decrease 이기 때문에 proxy의
        // start transaction 메서드(로직), end transaction 메서드(로직) 에는 영향을 미치지 못함
        // => decrease가 끝나면 순차적으로 다른 스레드가 decrease 실행)
        // 또 또 감소가 누락되게 된다

        // @Transactional 이 적용되면서 service가 proxy로 대체되면서 생기는 문제라
        // @Transactional 을 주석처리하면 정상적으로 작동함

        // 하지만 이 마저도 완벽한 대안이 되지 못하는데
        // synchronized 키워드는 하나의 서버에서만 보장된다
        // 즉, 여러대의 서버에서 동시에 재고 감소 로직을 실행시키게 되면
        // synchronized 는 순차성을 보장하지 못하게 되기 때문에 데이터에 여전히 동시 접근을 하게 되고
        // 재고 감소가 누락되는 일이 발생하게 된다.
        // 따라서 실무에서 적용하기엔 무리가 있다.

    }

    


}