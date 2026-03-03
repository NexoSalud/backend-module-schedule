package com.reactive.nexo.repository;

import com.reactive.nexo.model.Schedule;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface ScheduleRepository extends R2dbcRepository<Schedule, Long> {
    
    Flux<Schedule> findByEmployeeId(Long employeeId);
    
    Flux<Schedule> findByUserId(Long userId);
    
    @Query("SELECT COUNT(*) FROM schedule WHERE employee_id = :employeeId AND " +
           "((start_at <= :startAt AND end_at > :startAt) OR " +
           "(start_at < :endAt AND end_at >= :endAt) OR " +
           "(start_at >= :startAt AND end_at <= :endAt)) AND " +
           "(:id IS NULL OR id != :id)")
    Mono<Long> countOverlappingSchedulesForEmployee(
            @Param("employeeId") Long employeeId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("id") Long id);
    
    @Query("SELECT COUNT(*) FROM schedule WHERE user_id = :userId AND " +
           "((start_at <= :startAt AND end_at > :startAt) OR " +
           "(start_at < :endAt AND end_at >= :endAt) OR " +
           "(start_at >= :startAt AND end_at <= :endAt)) AND " +
           "(:id IS NULL OR id != :id)")
    Mono<Long> countOverlappingSchedulesForUser(
            @Param("userId") Long userId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("id") Long id);
    
    @Query("SELECT * FROM schedule WHERE employee_id = :employeeId AND " +
           "start_at = :startAt AND end_at = :endAt AND " +
           "group_session = true AND " +
           "(:id IS NULL OR id != :id)")
    Flux<Schedule> findExactGroupSession(
            @Param("employeeId") Long employeeId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("id") Long id);

       Flux<Schedule> findByStartAtGreaterThanEqual(LocalDateTime startAt);

       Flux<Schedule> findByEndAtLessThanEqual(LocalDateTime endAt);

       Flux<Schedule> findByStartAtGreaterThanEqualAndEndAtLessThanEqual(LocalDateTime startAt, LocalDateTime endAt);

       Mono<Long> countByStartAtGreaterThanEqual(LocalDateTime startAt);

       Mono<Long> countByEndAtLessThanEqual(LocalDateTime endAt);

       Mono<Long> countByStartAtGreaterThanEqualAndEndAtLessThanEqual(LocalDateTime startAt, LocalDateTime endAt);

       @Query("SELECT * FROM schedule WHERE " +
              "(:startAt IS NULL OR start_at >= :startAt) AND " +
              "(:endAt IS NULL OR end_at <= :endAt) AND " +
              "(:officeId IS NULL OR office_id = :officeId) AND " +
              "(:agendaId IS NULL OR agenda_id = :agendaId) " +
              "ORDER BY start_at ASC OFFSET :offset LIMIT :limit")
       Flux<Schedule> findAllWithFilters(@Param("startAt") LocalDateTime startAt, 
                                         @Param("endAt") LocalDateTime endAt, 
                                         @Param("officeId") Long officeId, 
                                         @Param("agendaId") Long agendaId,
                                         @Param("limit") int limit,
                                         @Param("offset") int offset);

       @Query("SELECT count(*) FROM schedule WHERE " +
              "(:startAt IS NULL OR start_at >= :startAt) AND " +
              "(:endAt IS NULL OR end_at <= :endAt) AND " +
              "(:officeId IS NULL OR office_id = :officeId) AND " +
              "(:agendaId IS NULL OR agenda_id = :agendaId)")
       Mono<Long> countWithFilters(@Param("startAt") LocalDateTime startAt, 
                                   @Param("endAt") LocalDateTime endAt, 
                                   @Param("officeId") Long officeId, 
                                   @Param("agendaId") Long agendaId);
}