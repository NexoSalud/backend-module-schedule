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
@Table("schedule_config")
public class ScheduleConfig {
    @Id
    private Long id;

    // Si configType es 'HOLIDAY', se usa este campo
    private LocalDate holidayDate;

    private String description;

    // default_start_time, default_end_time, etc. si aplica
    private String configType; // e.g. 'HOLIDAY', 'DEFAULT'

    private LocalDateTime createdAt;
}
