package com.reactive.nexo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitud para crear una Agenda Médica")
public class CreateMedicalAgendaRequest {
    @Schema(description = "ID del profesional", example = "1", required = true)
    private Long employeeId;

    @Schema(description = "Modalidad de atención", example = "intramural")
    private String modality; // intramural | telemedicina

    @Schema(description = "ID de sede (solo intramural)", example = "2")
    private Long headquartersId;

    @Schema(description = "ID de consultorio (solo intramural)", example = "201")
    private Long officeId;

    @Schema(description = "Fecha inicio vigencia", example = "2026-02-02")
    private String startDate; // YYYY-MM-DD

    @Schema(description = "Fecha fin vigencia", example = "2026-12-31")
    private String endDate;   // YYYY-MM-DD

    @Schema(description = "Días laborales", example = "[\"L\",\"M\",\"X\",\"J\",\"V\"]")
    private List<String> workDays; // L,M,X,J,V

    @Schema(description = "Hora inicio", example = "08:00")
    private String startTime; // HH:mm

    @Schema(description = "Hora fin", example = "17:00")
    private String endTime;   // HH:mm

    @Schema(description = "Duración de la cita en minutos", example = "20")
    private Integer appointmentDuration;

    @Schema(description = "IDs de tipos de servicio", example = "[1,2,3]")
    private List<Integer> serviceTypeIds;

    @Schema(description = "Permite agenda grupal", example = "false")
    private Boolean allowGroupSession;

    @Schema(description = "Requiere orden médica", example = "false")
    private Boolean requiresReferral;

    @Schema(description = "Notas u observaciones")
    private String notes;
}
