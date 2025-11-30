package com.reactive.nexo.service;

import com.reactive.nexo.dto.CreateScheduleRequest;
import com.reactive.nexo.dto.PagedResponse;
import com.reactive.nexo.model.Schedule;
import com.reactive.nexo.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleService {
    
    private final ScheduleRepository scheduleRepository;
    
    public Flux<Schedule> getAllSchedules() {
        return scheduleRepository.findAll();
    }
    
    public Mono<PagedResponse<Schedule>> getAllSchedules(int page, int size) {
        return scheduleRepository.count()
                .flatMap(totalElements -> {
                    PageRequest pageRequest = PageRequest.of(page, size);
                    return scheduleRepository.findAll()
                            .skip(page * size)
                            .take(size)
                            .collectList()
                            .map(content -> {
                                long totalPages = (totalElements + size - 1) / size;
                                boolean isLast = page >= totalPages - 1;
                                return new PagedResponse<>(content, page, size, totalElements, totalPages, isLast);
                            });
                });
    }
    
    public Mono<Schedule> getScheduleById(Long id) {
        return scheduleRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule not found")));
    }
    
    public Flux<Schedule> getSchedulesByEmployeeId(Long employeeId) {
        return scheduleRepository.findByEmployeeId(employeeId);
    }
    
    public Flux<Schedule> getSchedulesByUserId(Long userId) {
        return scheduleRepository.findByUserId(userId);
    }
    
    public Mono<Schedule> createSchedule(CreateScheduleRequest request) {
        if (request.getStartAt().isAfter(request.getEndAt())) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start time must be before end time"));
        }
        
        return validateNoOverlap(request.getEmployeeId(), request.getUserId(), 
                               request.getStartAt(), request.getEndAt(), null)
                .then(scheduleRepository.save(new Schedule(
                        request.getEmployeeId(),
                        request.getUserId(),
                        request.getStartAt(),
                        request.getEndAt(),
                        request.getDetails()
                )));
    }
    
    public Mono<Schedule> updateSchedule(Long id, CreateScheduleRequest request) {
        if (request.getStartAt().isAfter(request.getEndAt())) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start time must be before end time"));
        }
        
        return scheduleRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule not found")))
                .flatMap(existingSchedule -> 
                        validateNoOverlap(request.getEmployeeId(), request.getUserId(), 
                                        request.getStartAt(), request.getEndAt(), id)
                                .then(Mono.defer(() -> {
                                    existingSchedule.setEmployeeId(request.getEmployeeId());
                                    existingSchedule.setUserId(request.getUserId());
                                    existingSchedule.setStartAt(request.getStartAt());
                                    existingSchedule.setEndAt(request.getEndAt());
                                    existingSchedule.setDetails(request.getDetails());
                                    existingSchedule.setUpdatedAt(LocalDateTime.now());
                                    return scheduleRepository.save(existingSchedule);
                                }))
                );
    }
    
    public Mono<Void> deleteSchedule(Long id) {
        return scheduleRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule not found")))
                .flatMap(schedule -> scheduleRepository.delete(schedule));
    }
    
    private Mono<Void> validateNoOverlap(Long employeeId, Long userId, 
                                       LocalDateTime startAt, LocalDateTime endAt, Long excludeId) {
        Mono<Long> employeeOverlapCheck = scheduleRepository.countOverlappingSchedulesForEmployee(
                employeeId, startAt, endAt, excludeId);
        
        Mono<Long> userOverlapCheck = scheduleRepository.countOverlappingSchedulesForUser(
                userId, startAt, endAt, excludeId);
        
        return Mono.zip(employeeOverlapCheck, userOverlapCheck)
                .flatMap(tuple -> {
                    Long employeeOverlaps = tuple.getT1();
                    Long userOverlaps = tuple.getT2();
                    
                    if (employeeOverlaps > 0) {
                        return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, 
                                "Employee has overlapping schedule"));
                    }
                    
                    if (userOverlaps > 0) {
                        return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, 
                                "User has overlapping schedule"));
                    }
                    
                    return Mono.empty();
                });
    }
}