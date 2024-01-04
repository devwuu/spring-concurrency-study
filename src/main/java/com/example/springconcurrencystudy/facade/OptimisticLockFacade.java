package com.example.springconcurrencystudy.facade;

import com.example.springconcurrencystudy.service.OptimisticLockStockService;
import org.springframework.stereotype.Component;

@Component
public class OptimisticLockFacade {

    private final OptimisticLockStockService service;

    public OptimisticLockFacade(OptimisticLockStockService service) {
        this.service = service;
    }

    public void decrease(Long id, Long quantity) throws InterruptedException {
        while (true){ // update를 실패했을 때(version이 일치하지 않는 경우) 재시도하게 한다
            try {
                service.decrease(id, quantity); // 감소 시도
                break; // 정상적으로 감소가 되었을 경우 while문 탈출
            }catch (Exception e){
                Thread.sleep(50); // 50ms 이후 재시도
            }
        }
    }




}
