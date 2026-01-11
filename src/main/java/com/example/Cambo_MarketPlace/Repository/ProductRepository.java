package com.example.Cambo_MarketPlace.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.Cambo_MarketPlace.Models.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findBySellerId(Long sellerId);
}
