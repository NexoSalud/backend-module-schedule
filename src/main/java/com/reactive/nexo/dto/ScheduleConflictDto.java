package com.reactive.nexo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleConflictDto {
    private Long id;
    private String type; // OVERLAP | DUPLICATE | EXCEEDS_HOURS
    private String message;
    private Long conflictingAgendaId;
    private String conflictingAgendaName; // opcional
    private String date; // YYYY-MM-DD
    private String startTime; // HH:mm
    private String endTime;   // HH:mm
}
