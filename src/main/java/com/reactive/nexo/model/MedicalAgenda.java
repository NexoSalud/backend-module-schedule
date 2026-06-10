package com.reactive.nexo.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("medical_agenda")
@Schema(description = "Disponibilidad de un profesional (Agenda Médica)")
public class MedicalAgenda {
    @Id
    private Long id;

    private Long employeeId;
    private String modality; // intramural | telemedicina
    private Long headquartersId;
    private Long officeId;

    private LocalDate startDate; // DATE
    private LocalDate endDate; // DATE
    private String workDays; // CSV: L,M,X,J,V
    private String startTime; // HH:mm
    private String endTime; // HH:mm
    private Integer appointmentDuration;

    private String serviceTypes; // JSON/CSV
    private Boolean allowGroupSession;
    private Boolean requiresReferral;
    private String notes;

    private String status; // VIGENTE | CON_CONFLICTOS | PROXIMA_A_VENCER | VENCIDA | INACTIVA
    private Boolean isActive;
    private Boolean enabled;

    // Novedades Phase 1
    private String convenios; // JSON/CSV de convenios
    private Boolean canCreateMedicalHistory;
    private Boolean doubleShift;

    // Novedades Phase 2
    private Integer enabledSlots;

    // Novedades // Phase 3
    private String agendaState; // ABIERTA, CERRADA
    private String createdBy; // Identifier del creador

    // Phase 5
    private String frequency;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt; // TIMESTAMP
}
