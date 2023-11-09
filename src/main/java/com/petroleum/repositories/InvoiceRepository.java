package com.petroleum.repositories;

import com.petroleum.enums.Status;
import com.petroleum.models.Invoice;
import com.petroleum.models.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long>, PagingAndSortingRepository<Invoice, Long> {
    Page<Invoice> findAllByOrderByDateDesc(Pageable pageable);
    List<Invoice> findAllByProductAndStatusAndDateBetween(Product product, Status status, LocalDateTime start, LocalDateTime end);
    @Transactional
    @Modifying(clearAutomatically = true)
    void deleteAllByIdInAndStatusNot(Collection<Long> ids, Status status);
}
