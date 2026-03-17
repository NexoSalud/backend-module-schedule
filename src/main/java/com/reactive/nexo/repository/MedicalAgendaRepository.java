package com.reactive.nexo.repository;

import com.reactive.nexo.model.MedicalAgenda;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface MedicalAgendaRepository extends R2dbcRepository<MedicalAgenda, Long> {

    Flux<MedicalAgenda> findByEmployeeId(Long employeeId);

    Flux<MedicalAgenda> findByIsActive(Boolean isActive);

    Flux<MedicalAgenda> findByEnabled(Boolean enabled);

    @Query("SELECT * FROM medical_agenda WHERE office_id = :officeId AND start_date <= :endDate AND end_date >= :startDate AND is_active = true")
    Flux<MedicalAgenda> findByOfficeIdAndDateRange(@Param("officeId") Long officeId,
            @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(*) FROM medical_agenda")
    Mono<Long> countAll();

    @Query("SELECT COUNT(*) FROM medical_agenda WHERE employee_id = :employeeId")
    Mono<Long> countByEmployeeId(@Param("employeeId") Long employeeId);

    @Query("SELECT * FROM medical_agenda ORDER BY id DESC")
    Flux<MedicalAgenda> findAllOrdered();

    @Query("SELECT * FROM medical_agenda WHERE employee_id = :employeeId ORDER BY id DESC")
    Flux<MedicalAgenda> findByEmployeeIdOrdered(@Param("employeeId") Long employeeId);
}
