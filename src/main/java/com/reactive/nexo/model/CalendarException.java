package com.reactive.nexo.model;

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
@Table("calendar_exception")
public class CalendarException {
    @Id
    private Long id;
    private Long employeeId;
    private LocalDate exceptionDate;
    private String startTime;
    private String endTime;
    private String reason;
    private LocalDateTime createdAt;
}
