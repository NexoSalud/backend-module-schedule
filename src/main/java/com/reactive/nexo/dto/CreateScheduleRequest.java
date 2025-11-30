package com.reactive.nexo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateScheduleRequest {
    private Long employeeId;
    private Long userId;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String details;
}