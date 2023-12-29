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
    public void concurrencyTest() throws InterruptedException {
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
        //	at java.base/jdk.internal.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
        //	at java.base/jdk.internal.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:77)
        //	at java.base/jdk.internal.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:45)

        // SELECT 시에는 lock이 필요하지 않기 때문에 순차적으로 실행되는 것이 아니라
        // 같은 숫자에서 decrease되어 감소값이 누락되는 일이 발생한다.


    }

    


}