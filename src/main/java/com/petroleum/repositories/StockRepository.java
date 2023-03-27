package com.petroleum.repositories;

import com.petroleum.models.Depot;
import com.petroleum.models.Product;
import com.petroleum.models.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findFirstByDepotAndProduct(Depot depot, Product product);
}
