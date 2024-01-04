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
    // select s1_0.id,s1_0.product_id,s1_0.quantity from stock s1_0 where s1_0.id=? for update
    // 실제로 lock을 거는 것이기 때문에 데이터 정합성이 보장됩니다.
    // 또한 레이스 컨디션이 잦은 상황에서 낙관적 락보다 성능면에서 유리할 수 있습니다.
    // 단, 실제로 lock을 거는 것이기 때문에 데드락이 발생할 수 있고 성능이 감소할 가능성이 있습니다.

    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT s FROM Stock s WHERE s.id = :id")
    Stock findByIdWithOptimisticLock(Long id);
    // 낙관적 락?
    // version을 이용해서 update 할 때 데이터가 변경되어있는지 확인 하고 version이 변경되었으면
    // 해당 데이터를 다시 읽어 변경을 다시 시도하도록 로직을 작성한다.
    // update stock set product_id=?,quantity=?,version=? where id=? and version=?
    // select s1_0.id,s1_0.product_id,s1_0.quantity,s1_0.version from stock s1_0 where s1_0.id=?
    // select s1_0.id,s1_0.product_id,s1_0.quantity,s1_0.version from stock s1_0 where s1_0.id=?
    // update stock set product_id=?,quantity=?,version=? where id=? and version=?
    // update stock set product_id=?,quantity=?,version=? where id=? and version=?
    // ...
    // 실제로 db의 락을 거는 것이 아니기 때문에 성능상 장점이 있다.
    // 단, update 가 실패했을 때 재시도 하는 로직을 개발자가 별도로 작성해줘야 한다
    // 또한 충돌이 빈번한 데이터일 경우 비관적 락보다 성능이 떨어질 수 있다


}
