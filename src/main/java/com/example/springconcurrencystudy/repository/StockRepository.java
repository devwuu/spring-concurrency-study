package com.example.springconcurrencystudy.repository;

import com.example.springconcurrencystudy.domain.Stock;
import org.springframework.data.jpa.repository.JpaRepository;


public interface StockRepository extends JpaRepository<Stock, Long> {
}
