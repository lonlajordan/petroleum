package com.petroleum.repositories;

import com.petroleum.models.Fuel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface FuelRepository extends JpaRepository<Fuel, Long>, PagingAndSortingRepository<Fuel, Long> {
    Page<Fuel> findAllByOrderByDateDesc(Pageable pageable);
    Page<Fuel> findAllByProductIdOrderByDateDesc(long fuelId, Pageable pageable);
    Fuel findByCodeAndNumber(String code, int number);
}
