package com.reactive.nexo.model;

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
public class Schedule {
    @Id
    private Long id;
    private Long employeeId;
    private Long userId;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String details;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Schedule(Long employeeId, Long userId, LocalDateTime startAt, LocalDateTime endAt, String details) {
        this.employeeId = employeeId;
        this.userId = userId;
        this.startAt = startAt;
        this.endAt = endAt;
        this.details = details;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}