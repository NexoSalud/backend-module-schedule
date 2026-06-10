package com.reactive.nexo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitud para crear o actualizar una excepción de calendario")
public class CalendarExceptionRequest {

    @Schema(description = "ID del empleado", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long employeeId;

    @Schema(description = "Fecha de la excepción (YYYY-MM-DD)", example = "2024-12-25", requiredMode = Schema.RequiredMode.REQUIRED)
    private String exceptionDate;

    @Schema(description = "Hora de inicio del bloqueo (HH:mm). Si es nulo o vacío, el bloqueo es para todo el día.", example = "08:00")
    private String startTime;

    @Schema(description = "Hora de fin del bloqueo (HH:mm). Si es nulo o vacío, el bloqueo es para todo el día o desde la hora de inicio.", example = "12:00")
    private String endTime;

    @Schema(description = "Razón o motivo del bloqueo", example = "Vacaciones")
    private String reason;
}
