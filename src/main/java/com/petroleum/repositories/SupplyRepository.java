package com.petroleum.repositories;

import com.petroleum.models.Supply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplyRepository extends JpaRepository<Supply, Long>, PagingAndSortingRepository<Supply, Long> {
    Page<Supply> findAllByOrderByDateDesc(Pageable pageable);
    Page<Supply> findAllByProductIdOrderByDateDesc(long productId, Pageable pageable);
}
