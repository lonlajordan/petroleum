package com.petroleum.repositories;

import com.petroleum.models.Log;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface LogRepository  extends JpaRepository<Log, Long>, PagingAndSortingRepository<Log, Long> {
    Page<Log> findAllByOrderByDateDesc(Pageable pageable);
    int countAllByMessageContaining(String message);
}
