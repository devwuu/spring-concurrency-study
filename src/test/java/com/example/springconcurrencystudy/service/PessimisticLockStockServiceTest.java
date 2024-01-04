package com.example.springconcurrencystudy.service;

import com.example.springconcurrencystudy.domain.Stock;
import com.example.springconcurrencystudy.repository.StockRepository;
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
class PessimisticLockStockServiceTest {

    @Autowired
    PessimisticLockStockService service;
    @Autowired
    StockRepository repository;

    @BeforeEach
    public void init(){
        repository.saveAndFlush(new Stock(1L, 100L));
    }

    @AfterEach
    public void after(){
        repository.deleteAll();
    }

    @Test
    @DisplayName("비관적 락을 이용해 문제를 해결합니다")
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

    }

}