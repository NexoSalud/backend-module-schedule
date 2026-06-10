package com.reactive.nexo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalendarEventDto {
    private Long id;
    private String date; // YYYY-MM-DD
    private String type; // DISPONIBLE | CONFLICTO | etc.
    private String title;
    private String startTime; // HH:mm
    private String endTime;   // HH:mm
    private String details;
}
