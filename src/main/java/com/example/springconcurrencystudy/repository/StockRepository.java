package com.example.springconcurrencystudy.repository;

import com.example.springconcurrencystudy.domain.Stock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;


public interface StockRepository extends JpaRepository<Stock, Long> {


    @Lock(LockModeType.PESSIMISTIC_WRITE) // JPA 가 제공하는 Pessimistic lock 구현 방법
    @Query("SELECT s FROM Stock s WHERE s.id = :id")
    Stock findByIdWithPessimisticLock(Long id);

    // 비관적 락?
    // db에서 해당 데이터에 실제로 lock을 걸어서 해당 데이터에 걸린 lock이 해제되기 전에
    // 해당 데이터에 접근할 수 없도록 합니다.
    // 실제로 lock을 거는 것이기 때문에 데이터 정합성이 보장됩니다.
    // 또한 레이스 컨디션이 잦은 상황에서 낙관적 락보다 성능면에서 유리할 수 있습니다.
    // 단, 실제로 lock을 거는 것이기 때문에 데드락이 발생할 수 있고 성능이 감소할 가능성이 있습니다.


}
