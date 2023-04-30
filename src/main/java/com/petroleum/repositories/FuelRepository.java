package com.petroleum.repositories;

import com.petroleum.models.Fuel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FuelRepository extends JpaRepository<Fuel, Long>, PagingAndSortingRepository<Fuel, Long> {
    Page<Fuel> findAllByOrderByDateDesc(Pageable pageable);
    Fuel findByCodeAndNumber(String code, int number);
    Fuel findByAmountAndNumber(double amount, int number);
    List<Fuel> findAllByAmountOrderByNumberAsc(double amount);
}
