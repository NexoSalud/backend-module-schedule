package com.reactive.nexo.controller;

import com.reactive.nexo.dto.MedicalAgendaView;
import com.reactive.nexo.dto.CalendarEventDto;
import com.reactive.nexo.dto.ScheduleConflictDto;
import com.reactive.nexo.dto.PagedResponse;
import com.reactive.nexo.dto.CreateMedicalAgendaRequest;
import com.reactive.nexo.model.MedicalAgenda;
import com.reactive.nexo.service.MedicalAgendaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1/medical-agenda")
@RequiredArgsConstructor
@Tag(name = "MedicalAgenda", description = "API de Agendas Médicas (disponibilidad de profesionales)")
public class MedicalAgendaController {

    private final MedicalAgendaService service;

    @Operation(summary = "Listar agendas médicas", description = "Devuelve una página de agendas con filtros opcionales")
    @ApiResponse(responseCode = "200", description = "Listado obtenido")
    @GetMapping
    public Mono<ResponseEntity<PagedResponse<MedicalAgendaView>>> list(
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @RequestParam(name = "employeeId", required = false) Long employeeId,
            @RequestHeader HttpHeaders headers
    ) {
        return service.listAgendas(page, size, employeeId, headers)
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Obtener agenda médica por ID")
    @ApiResponse(responseCode = "200", description = "Agenda encontrada")
    @GetMapping("/{id}")
    public Mono<ResponseEntity<MedicalAgendaView>> getById(@PathVariable Long id, @RequestHeader HttpHeaders headers) {
        return service.getById(id, headers).map(ResponseEntity::ok);
    }

    @Operation(summary = "Crear agenda médica")
    @ApiResponse(responseCode = "201", description = "Agenda creada")
    @PostMapping
    public Mono<ResponseEntity<MedicalAgenda>> create(@RequestBody CreateMedicalAgendaRequest payload) {
        return service.createFromDto(payload)
                .map(created -> ResponseEntity.status(201).body(created));
    }

    @Operation(summary = "Actualizar agenda médica")
    @ApiResponse(responseCode = "200", description = "Agenda actualizada")
    @PutMapping("/{id}")
    public Mono<ResponseEntity<MedicalAgenda>> update(@PathVariable Long id, @RequestBody MedicalAgenda payload) {
        return service.update(id, payload).map(ResponseEntity::ok);
    }

    @Operation(summary = "Actualizar parcialmente una agenda médica")
    @ApiResponse(responseCode = "200", description = "Agenda actualizada")
    @PatchMapping("/{id}")
    public Mono<ResponseEntity<MedicalAgenda>> patch(@PathVariable Long id, @RequestBody MedicalAgenda payload) {
        return service.patch(id, payload).map(ResponseEntity::ok);
    }

    @Operation(summary = "Cambiar estado enabled de una agenda médica")
    @ApiResponse(responseCode = "200", description = "Estado cambiado")
    @PatchMapping("/{id}/enabled")
    public Mono<ResponseEntity<MedicalAgenda>> updateEnabled(@PathVariable Long id, @RequestParam Boolean enabled) {
        return service.updateEnabled(id, enabled).map(ResponseEntity::ok);
    }

    @Operation(summary = "Eliminar agenda médica")
    @ApiResponse(responseCode = "204", description = "Agenda eliminada")
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable Long id) {
        return service.delete(id).then(Mono.just(ResponseEntity.noContent().build()));
    }

    @Operation(summary = "Eventos de calendario por empleado")
    @ApiResponse(responseCode = "200", description = "Eventos obtenidos")
    @GetMapping("/calendar-events")
    public Mono<ResponseEntity<java.util.List<CalendarEventDto>>> calendarEvents(
            @RequestParam Long employeeId,
            @RequestParam String startDate,
            @RequestParam String endDate
    ) {
        return service.getCalendarEvents(employeeId, startDate, endDate)
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Verificar conflictos de agenda")
    @ApiResponse(responseCode = "200", description = "Conflictos devueltos")
    @PostMapping("/check-conflicts")
    public Mono<ResponseEntity<java.util.List<ScheduleConflictDto>>> checkConflicts(@RequestBody MedicalAgenda payload) {
        return service.checkConflicts(payload).map(ResponseEntity::ok);
    }
}
