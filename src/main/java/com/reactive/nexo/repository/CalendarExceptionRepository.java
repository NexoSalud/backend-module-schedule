package com.reactive.nexo.repository;

import com.reactive.nexo.model.CalendarException;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

public interface CalendarExceptionRepository extends ReactiveCrudRepository<CalendarException, Long> {

    @Query("SELECT * FROM calendar_exception WHERE employee_id = :employeeId AND exception_date >= :startDate AND exception_date <= :endDate")
    Flux<CalendarException> findByEmployeeIdAndDateRange(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
