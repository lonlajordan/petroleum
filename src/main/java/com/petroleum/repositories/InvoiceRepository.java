package com.petroleum.repositories;

import com.petroleum.enums.Status;
import com.petroleum.models.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

public interface InvoiceRepository extends JpaRepository<Invoice, Long>, PagingAndSortingRepository<Invoice, Long> {
    Page<Invoice> findAllByOrderByDateDesc(Pageable pageable);
    @Query("SELECT coalesce(sum(i.volume), 0) FROM Invoice i WHERE i.product.id = ?1 AND i.status = ?2")
    int sumProductVolumeByStatus(long productId, Status status);
    @Transactional
    @Modifying(clearAutomatically = true)
    int deleteAllByIdInAndStatusNot(Collection<Long> ids, Status status);
}
