package com.reactive.nexo.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("schedule")
@Schema(description = "Entidad que representa una cita programada")
public class Schedule {
    @Id
    @Schema(description = "ID único de la cita", example = "1")
    private Long id;
    
    @Schema(description = "ID del empleado que atenderá", example = "1")
    private Long employeeId;
    
    @Schema(description = "ID del usuario de la cita", example = "1")
    private Long userId;
    
    @Schema(description = "Fecha y hora de inicio", example = "2024-02-15T09:00:00")
    private LocalDateTime startAt;
    
    @Schema(description = "Fecha y hora de fin", example = "2024-02-15T10:00:00")
    private LocalDateTime endAt;
    
    @Schema(description = "Detalles adicionales", example = "Consulta médica general")
    private String details;
    
    @Schema(description = "Sede donde se realizará la cita", example = "Sede Central")
    private String headquarters;

    @Schema(description = "Oficina donde se realizará la cita", example = "Oficina 101")
    private String office;

    @Schema(description = "Indica si la cita es presencial", example = "false")
    private Boolean inPerson;

    @Schema(description = "Indica si es una sesión grupal", example = "false")
    private Boolean groupSession;
    
    @Schema(description = "Fecha de creación del registro")
    private LocalDateTime createdAt;
    
    @Schema(description = "Fecha de última actualización")
    private LocalDateTime updatedAt;

    public Schedule(Long employeeId, Long userId, LocalDateTime startAt, LocalDateTime endAt, String details, String headquarters, String office, Boolean inPerson, Boolean groupSession) {
        this.employeeId = employeeId;
        this.userId = userId;
        this.startAt = startAt;
        this.endAt = endAt;
        this.details = details;
        this.headquarters = headquarters;
        this.office = office;
        this.inPerson = inPerson != null ? inPerson : false;
        this.groupSession = groupSession != null ? groupSession : false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public Schedule(Long employeeId, Long userId, LocalDateTime startAt, LocalDateTime endAt, String details, String headquarters, String office) {
        this(employeeId, userId, startAt, endAt, details, headquarters, office, false, false);
    }

    public Schedule(Long employeeId, Long userId, LocalDateTime startAt, LocalDateTime endAt, String details, Boolean groupSession) {
        this(employeeId, userId, startAt, endAt, details, null, null, false, groupSession);
    }
}