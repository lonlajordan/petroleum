package com.petroleum.repositories;

import com.petroleum.models.Fuel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FuelRepository extends JpaRepository<Fuel, Long>, PagingAndSortingRepository<Fuel, Long> {
    Page<Fuel> findAllByOrderByDateDesc(Pageable pageable);
    Fuel findByCodeAndNumber(String code, int number);
    Fuel findByAmountAndNumber(double amount, int number);
    @Query("SELECT COALESCE(MAX(f.number), 0) FROM Fuel f WHERE f.amount = ?1")
    int findMaxNumberByAmount(double amount);
    List<Fuel> findAllByAmountAndNumberBetweenOrderByNumberAsc(double amount, int min, int max);
}
