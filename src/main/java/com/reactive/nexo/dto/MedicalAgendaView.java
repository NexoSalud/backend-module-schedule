package com.reactive.nexo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicalAgendaView {
    public Long id;
    public Long employeeId;
    public EmployeeDto employee; // Datos mínimos para el listado
    public String modality;
    public Long headquartersId;
    public Long officeId;
    public String startDate;
    public String endDate;
    public String workDays;
    public String startTime;
    public String endTime;
    public Integer appointmentDuration;
    public String serviceTypes;
    public Boolean allowGroupSession;
    public Boolean requiresReferral;
    public String notes;
    public String status;
    public Boolean isActive;
    public String createdAt;
    public String updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployeeDto {
        public Long id;
        public String names;
        public String lastnames;
        public String identification_type;
        public String identification_number;
        public Integer rol_id;
        public Boolean is_active;
    }
}
