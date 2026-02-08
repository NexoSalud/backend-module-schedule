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
}
