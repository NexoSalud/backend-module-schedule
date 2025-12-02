package com.reactive.nexo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Solicitud para crear o actualizar una cita")
public class CreateScheduleRequest {
    @Schema(description = "ID del empleado que atenderá la cita", example = "1", required = true)
    private Long employeeId;
    
    @Schema(description = "ID del usuario que tendrá la cita", example = "1", required = true)
    private Long userId;
    
    @Schema(description = "Fecha y hora de inicio de la cita", example = "2024-02-15T09:00:00", required = true)
    private LocalDateTime startAt;
    
    @Schema(description = "Fecha y hora de fin de la cita", example = "2024-02-15T10:00:00", required = true)
    private LocalDateTime endAt;
    
    @Schema(description = "Detalles adicionales de la cita", example = "Consulta médica general")
    private String details;
    
    @Schema(description = "Indica si es una sesión grupal que permite múltiples usuarios en la misma hora", 
            example = "false", defaultValue = "false")
    private Boolean groupSession;
}