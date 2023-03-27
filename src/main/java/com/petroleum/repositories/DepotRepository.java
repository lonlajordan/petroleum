package com.petroleum.repositories;

import com.petroleum.models.Depot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepotRepository extends JpaRepository<Depot, Long> {
    List<Depot> findAllByOrderByNameAsc();
}
