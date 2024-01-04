package com.example.springconcurrencystudy.repository;

import com.example.springconcurrencystudy.domain.Stock;
import com.example.springconcurrencystudy.domain.StockLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

// named lock을 위한 레포지토리
// 실무에서는 실제로 사용할 엔터디(Stock)에 바로 붙는 것이 아니라 lock 전용 data(StockLock)를 따로 만드는 것을 추천한다
// 또한 datasource (connection pool)을 완전히 분리해서
// lock을 얻기 위해 대기하는 스레드가 connection pool을 고갈시키지 않도록 한다.
public interface LockRepository extends JpaRepository<StockLock, Long> {
    @Query(value = "SELECT get_lock(:key, 3000)", nativeQuery = true)
    void getLock(String key);

    @Query(value = "SELECT release_lock(:key)", nativeQuery = true)
    void releaseLock(String key);

    // named lock
    // 주로 분산락을 구현할 때 많이 사용한다
    // 비관적 락은 타임아웃을 구현하기 힘들지만 named lock은 타임아웃을 구현하기 용이하다
    // 데이터 정합성을 맞출 때 named 락을 사용할 수도 있다
    // 단, transaction 종료시 lock 해제, 세션관리를 잘 해줘야 하기 때문에 주의를 해야 한다.
    // 또한 실무에서 구현하는 방법은 어려울 수 있다.

}
