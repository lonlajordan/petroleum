package com.petroleum.repositories;

import com.petroleum.enums.Status;
import com.petroleum.models.Product;
import com.petroleum.models.Transfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, Long>, PagingAndSortingRepository<Transfer, Long> {
    Page<Transfer> findAllByOrderByDateDesc(Pageable pageable);
    List<Transfer> findAllByProductAndStatusAndDateBetween(Product product, Status status, LocalDateTime start, LocalDateTime end);
    @Transactional
    @Modifying(clearAutomatically = true)
    int deleteAllByIdInAndStatusNot(Collection<Long> ids, Status status);
}
