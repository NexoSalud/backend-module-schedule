package com.reactive.nexo.service;

import com.reactive.nexo.dto.CalendarExceptionRequest;
import com.reactive.nexo.model.CalendarException;
import com.reactive.nexo.repository.CalendarExceptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class CalendarExceptionService {

    @Autowired
    private CalendarExceptionRepository repository;

    public Flux<CalendarException> getExceptionsByEmployeeIdAndDateRange(Long employeeId, LocalDate startDate,
            LocalDate endDate) {
        return repository.findByEmployeeIdAndDateRange(employeeId, startDate, endDate);
    }

    public Mono<CalendarException> createException(CalendarExceptionRequest request) {
        CalendarException exception = new CalendarException();
        exception.setEmployeeId(request.getEmployeeId());
        exception.setExceptionDate(LocalDate.parse(request.getExceptionDate()));
        exception.setStartTime(request.getStartTime());
        exception.setEndTime(request.getEndTime());
        exception.setReason(request.getReason());
        exception.setCreatedAt(LocalDateTime.now());

        return repository.save(exception);
    }

    public Mono<Boolean> deleteException(Long id) {
        return repository.findById(id)
                .flatMap(exception -> repository.delete(exception).then(Mono.just(true)))
                .defaultIfEmpty(false);
    }
}
