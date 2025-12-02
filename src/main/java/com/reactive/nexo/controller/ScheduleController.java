package com.reactive.nexo.controller;

import com.reactive.nexo.dto.CreateScheduleRequest;
import com.reactive.nexo.dto.PagedResponse;
import com.reactive.nexo.model.Schedule;
import com.reactive.nexo.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/schedule")
@RequiredArgsConstructor
@Tag(name = "Schedule", description = "API para gestión de citas y horarios")
public class ScheduleController {
    
    private final ScheduleService scheduleService;
    
    @Operation(
        summary = "Obtener todas las citas",
        description = "Devuelve una lista paginada de todas las citas del sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de citas obtenida exitosamente")
    })
    @GetMapping
    public Mono<ResponseEntity<PagedResponse<Schedule>>> getAllSchedules(
            @Parameter(description = "Número de página (base 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        return scheduleService.getAllSchedules(page, size)
                .map(ResponseEntity::ok);
    }
    
    @Operation(
        summary = "Obtener cita por ID",
        description = "Devuelve una cita específica basada en su ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cita encontrada"),
        @ApiResponse(responseCode = "404", description = "Cita no encontrada")
    })
    @GetMapping("/{id}")
    public Mono<ResponseEntity<Schedule>> getScheduleById(
            @Parameter(description = "ID de la cita", required = true)
            @PathVariable Long id) {
        return scheduleService.getScheduleById(id)
                .map(ResponseEntity::ok);
    }
    
    @Operation(
        summary = "Obtener citas por empleado",
        description = "Devuelve todas las citas asignadas a un empleado específico"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de citas del empleado obtenida exitosamente")
    })
    @GetMapping("/employee/{employeeId}")
    public Flux<Schedule> getSchedulesByEmployeeId(
            @Parameter(description = "ID del empleado", required = true)
            @PathVariable Long employeeId) {
        return scheduleService.getSchedulesByEmployeeId(employeeId);
    }
    
    @Operation(
        summary = "Obtener citas por usuario",
        description = "Devuelve todas las citas de un usuario/paciente específico"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de citas del usuario obtenida exitosamente")
    })
    @GetMapping("/user/{userId}")
    public Flux<Schedule> getSchedulesByUserId(
            @Parameter(description = "ID del usuario/paciente", required = true)
            @PathVariable Long userId) {
        return scheduleService.getSchedulesByUserId(userId);
    }
    
    @Operation(
        summary = "Crear nueva cita",
        description = "Crea una nueva cita en el sistema. Valida que no haya solapamiento de horarios para sesiones individuales. " +
                     "Para sesiones grupales (groupSession=true), permite múltiples usuarios en la misma hora exacta con el mismo empleado."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Cita creada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos - hora de inicio debe ser anterior a hora de fin"),
        @ApiResponse(responseCode = "409", description = "Conflicto de horarios - cita solapada para sesiones individuales")
    })
    @PostMapping
    public Mono<ResponseEntity<Schedule>> createSchedule(
            @Parameter(description = "Datos de la nueva cita", required = true)
            @RequestBody CreateScheduleRequest request) {
        return scheduleService.createSchedule(request)
                .map(schedule -> ResponseEntity.status(HttpStatus.CREATED).body(schedule));
    }
    
    @Operation(
        summary = "Actualizar cita",
        description = "Actualiza una cita existente. Valida que no haya solapamiento de horarios"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cita actualizada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "404", description = "Cita no encontrada"),
        @ApiResponse(responseCode = "409", description = "Conflicto de horarios - cita solapada")
    })
    @PutMapping("/{id}")
    public Mono<ResponseEntity<Schedule>> updateSchedule(
            @Parameter(description = "ID de la cita a actualizar", required = true)
            @PathVariable Long id,
            @Parameter(description = "Nuevos datos de la cita", required = true)
            @RequestBody CreateScheduleRequest request) {
        return scheduleService.updateSchedule(id, request)
                .map(ResponseEntity::ok);
    }
    
    @Operation(
        summary = "Eliminar cita",
        description = "Elimina una cita del sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Cita eliminada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Cita no encontrada")
    })
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteSchedule(
            @Parameter(description = "ID de la cita a eliminar", required = true)
            @PathVariable Long id) {
        return scheduleService.deleteSchedule(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}