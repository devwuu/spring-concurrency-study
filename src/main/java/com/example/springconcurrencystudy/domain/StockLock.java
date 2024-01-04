package com.example.springconcurrencystudy.domain;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class StockLock {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

}
