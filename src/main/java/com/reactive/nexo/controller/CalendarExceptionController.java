package com.reactive.nexo.controller;

import com.reactive.nexo.dto.CalendarExceptionRequest;
import com.reactive.nexo.model.CalendarException;
import com.reactive.nexo.service.CalendarExceptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/calendar-exceptions")
@Tag(name = "Calendar Exceptions", description = "Endpoints for managing calendar exceptions (blocked days/hours) for professionals")
public class CalendarExceptionController {

    @Autowired
    private CalendarExceptionService service;

    @GetMapping
    @Operation(summary = "Get calendar exceptions for an employee within a date range")
    public Flux<CalendarException> getExceptions(
            @RequestParam Long employeeId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        return service.getExceptionsByEmployeeIdAndDateRange(employeeId, LocalDate.parse(startDate),
                LocalDate.parse(endDate));
    }

    @PostMapping
    @Operation(summary = "Create a new calendar exception")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<CalendarException> createException(@RequestBody CalendarExceptionRequest request) {
        return service.createException(request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a calendar exception by ID")
    public Mono<ResponseEntity<Void>> deleteException(@PathVariable Long id) {
        return service.deleteException(id)
                .map(deleted -> deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build());
    }
}
