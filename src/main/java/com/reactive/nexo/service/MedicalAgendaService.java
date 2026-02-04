package com.reactive.nexo.service;

import com.reactive.nexo.dto.MedicalAgendaView;
import com.reactive.nexo.dto.CalendarEventDto;
import com.reactive.nexo.dto.ScheduleConflictDto;
import com.reactive.nexo.dto.PagedResponse;
import com.reactive.nexo.dto.CreateMedicalAgendaRequest;
import com.reactive.nexo.model.MedicalAgenda;
import com.reactive.nexo.repository.MedicalAgendaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MedicalAgendaService {

    private final MedicalAgendaRepository repository;
    private final WebClient webClient = WebClient.create(System.getenv().getOrDefault("EMPLOYEES_SERVICE_URL", "http://localhost:8081"));

    @Value("${service.gateway.url:http://localhost:8080}")
    private String gatewayUrl;

    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public Mono<PagedResponse<MedicalAgendaView>> listAgendas(Integer page, Integer size, Long employeeId, HttpHeaders headers) {
        final int p = page != null ? page : 0;
        final int s = size != null ? size : 10;

        Mono<Long> countMono = (employeeId != null)
                ? repository.countByEmployeeId(employeeId)
                : repository.countAll();

        Flux<MedicalAgenda> src = (employeeId != null)
                ? repository.findByEmployeeIdOrdered(employeeId)
                : repository.findAllOrdered();

        return countMono.flatMap(total -> src
                .skip((long) p * s)
                .take(s)
                .flatMap(ma -> enrichWithEmployee(ma, headers))
                .collectList()
                .map(list -> new PagedResponse<>(list, p, s, total, (total + s - 1) / s, p >= ((total + s - 1) / s) - 1))
        );
    }

    public Mono<MedicalAgendaView> getById(Long id, HttpHeaders headers) {
        return repository.findById(id)
                .flatMap(ma -> enrichWithEmployee(ma, headers));
    }

    public Mono<MedicalAgenda> create(MedicalAgenda payload) {
        payload.setStatus(computeStatus(payload.getStartDate(), payload.getEndDate(), Boolean.TRUE.equals(payload.getIsActive())));
        payload.setCreatedAt(LocalDateTime.now());
        payload.setUpdatedAt(LocalDateTime.now());
        return repository.save(payload);
    }

    public Mono<MedicalAgenda> createFromDto(CreateMedicalAgendaRequest dto) {
        MedicalAgenda payload = new MedicalAgenda();
        payload.setEmployeeId(dto.getEmployeeId());
        payload.setModality(dto.getModality());
        payload.setHeadquartersId(dto.getHeadquartersId());
        payload.setOfficeId(dto.getOfficeId());
        payload.setStartDate(LocalDate.parse(dto.getStartDate(), ISO_DATE));
        payload.setEndDate(LocalDate.parse(dto.getEndDate(), ISO_DATE));
        // Convertir días laborales a CSV
        String workDaysCsv = (dto.getWorkDays() != null && !dto.getWorkDays().isEmpty())
                ? String.join(",", dto.getWorkDays())
                : "";
        payload.setWorkDays(workDaysCsv);
        payload.setStartTime(dto.getStartTime());
        payload.setEndTime(dto.getEndTime());
        payload.setAppointmentDuration(dto.getAppointmentDuration());
        // Convertir tipos de servicio a CSV (ids)
        String serviceTypesCsv = (dto.getServiceTypeIds() != null && !dto.getServiceTypeIds().isEmpty())
                ? dto.getServiceTypeIds().stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(","))
                : null;
        payload.setServiceTypes(serviceTypesCsv);
        payload.setAllowGroupSession(Boolean.TRUE.equals(dto.getAllowGroupSession()));
        payload.setRequiresReferral(Boolean.TRUE.equals(dto.getRequiresReferral()));
        payload.setNotes(dto.getNotes());
        payload.setIsActive(true); // por defecto activa

        payload.setStatus(computeStatus(payload.getStartDate(), payload.getEndDate(), Boolean.TRUE.equals(payload.getIsActive())));
        payload.setCreatedAt(LocalDateTime.now());
        payload.setUpdatedAt(LocalDateTime.now());
        return repository.save(payload);
    }

    public Mono<MedicalAgenda> update(Long id, MedicalAgenda payload) {
        return repository.findById(id)
                .flatMap(existing -> {
                    existing.setEmployeeId(payload.getEmployeeId());
                    existing.setModality(payload.getModality());
                    existing.setHeadquartersId(payload.getHeadquartersId());
                    existing.setOfficeId(payload.getOfficeId());
                    existing.setStartDate(payload.getStartDate());
                    existing.setEndDate(payload.getEndDate());
                    existing.setWorkDays(payload.getWorkDays());
                    existing.setStartTime(payload.getStartTime());
                    existing.setEndTime(payload.getEndTime());
                    existing.setAppointmentDuration(payload.getAppointmentDuration());
                    existing.setServiceTypes(payload.getServiceTypes());
                    existing.setAllowGroupSession(payload.getAllowGroupSession());
                    existing.setRequiresReferral(payload.getRequiresReferral());
                    existing.setNotes(payload.getNotes());
                    existing.setIsActive(payload.getIsActive());
                    existing.setStatus(computeStatus(existing.getStartDate(), existing.getEndDate(), Boolean.TRUE.equals(existing.getIsActive())));
                    existing.setUpdatedAt(LocalDateTime.now());
                    return repository.save(existing);
                });
    }

    public Mono<MedicalAgenda> patch(Long id, MedicalAgenda payload) {
        return repository.findById(id)
                .flatMap(existing -> {
                    if (payload.getEmployeeId() != null) existing.setEmployeeId(payload.getEmployeeId());
                    if (payload.getModality() != null) existing.setModality(payload.getModality());
                    if (payload.getHeadquartersId() != null) existing.setHeadquartersId(payload.getHeadquartersId());
                    if (payload.getOfficeId() != null) existing.setOfficeId(payload.getOfficeId());
                    if (payload.getStartDate() != null) existing.setStartDate(payload.getStartDate());
                    if (payload.getEndDate() != null) existing.setEndDate(payload.getEndDate());
                    if (payload.getWorkDays() != null) existing.setWorkDays(payload.getWorkDays());
                    if (payload.getStartTime() != null) existing.setStartTime(payload.getStartTime());
                    if (payload.getEndTime() != null) existing.setEndTime(payload.getEndTime());
                    if (payload.getAppointmentDuration() != null) existing.setAppointmentDuration(payload.getAppointmentDuration());
                    if (payload.getServiceTypes() != null) existing.setServiceTypes(payload.getServiceTypes());
                    if (payload.getAllowGroupSession() != null) existing.setAllowGroupSession(payload.getAllowGroupSession());
                    if (payload.getRequiresReferral() != null) existing.setRequiresReferral(payload.getRequiresReferral());
                    if (payload.getNotes() != null) existing.setNotes(payload.getNotes());
                    if (payload.getIsActive() != null) existing.setIsActive(payload.getIsActive());
                    existing.setStatus(computeStatus(existing.getStartDate(), existing.getEndDate(), Boolean.TRUE.equals(existing.getIsActive())));
                    existing.setUpdatedAt(LocalDateTime.now());
                    return repository.save(existing);
                });
    }

    public Mono<Void> delete(Long id) {
        return repository.deleteById(id);
    }

    public Mono<List<CalendarEventDto>> getCalendarEvents(Long employeeId, String startDate, String endDate) {
        LocalDate start = LocalDate.parse(startDate, ISO_DATE);
        LocalDate end = LocalDate.parse(endDate, ISO_DATE);
        return repository.findByEmployeeId(employeeId)
                .filter(ma -> isDateRangeOverlap(start, end, ma.getStartDate(), ma.getEndDate()))
                .collectList()
                .map(agendas -> {
                    List<CalendarEventDto> events = new ArrayList<>();
                    long counter = 1;
                    for (MedicalAgenda ma : agendas) {
                        Set<DayOfWeek> days = parseWorkDays(ma.getWorkDays());
                        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
                            LocalDate aStart = ma.getStartDate();
                            LocalDate aEnd = ma.getEndDate();
                            if (!d.isBefore(aStart) && !d.isAfter(aEnd) && days.contains(d.getDayOfWeek())) {
                                events.add(new CalendarEventDto(
                                        (ma.getId() != null ? ma.getId() : 0L) * 1_000_000 + counter++,
                                        d.format(ISO_DATE),
                                        "DISPONIBLE",
                                        "Disponibilidad",
                                        ma.getStartTime(),
                                        ma.getEndTime(),
                                        null
                                ));
                            }
                        }
                    }
                    return events;
                });
    }

    public Mono<List<ScheduleConflictDto>> checkConflicts(MedicalAgenda candidate) {
        return repository.findByEmployeeId(candidate.getEmployeeId())
                .filter(existing -> rangesOverlap(candidate, existing))
                .map(existing -> buildConflict(candidate, existing))
                .collectList();
    }

    private boolean rangesOverlap(MedicalAgenda a, MedicalAgenda b) {
        LocalDate aStart = a.getStartDate();
        LocalDate aEnd = a.getEndDate();
        LocalDate bStart = b.getStartDate();
        LocalDate bEnd = b.getEndDate();
        if (!isDateRangeOverlap(aStart, aEnd, bStart, bEnd)) return false;

        // Intersección de días de la semana
        Set<DayOfWeek> aDays = parseWorkDays(a.getWorkDays());
        Set<DayOfWeek> bDays = parseWorkDays(b.getWorkDays());
        boolean dayOverlap = aDays.stream().anyMatch(bDays::contains);
        if (!dayOverlap) return false;

        // Solapamiento de tiempo en el día (HH:mm)
        return timeRangesOverlap(a.getStartTime(), a.getEndTime(), b.getStartTime(), b.getEndTime());
    }

    private boolean isDateRangeOverlap(LocalDate s1, LocalDate e1, LocalDate s2, LocalDate e2) {
        return !s1.isAfter(e2) && !s2.isAfter(e1);
    }

    private boolean timeRangesOverlap(String s1, String e1, String s2, String e2) {
        int aStart = toMinutes(s1);
        int aEnd = toMinutes(e1);
        int bStart = toMinutes(s2);
        int bEnd = toMinutes(e2);
        return aStart < bEnd && bStart < aEnd;
    }

    private int toMinutes(String hhmm) {
        try {
            String[] p = hhmm.split(":");
            return Integer.parseInt(p[0]) * 60 + Integer.parseInt(p[1]);
        } catch (Exception e) {
            return 0;
        }
    }

    private Set<DayOfWeek> parseWorkDays(String workDays) {
        Set<DayOfWeek> set = new HashSet<>();
        if (workDays == null || workDays.isEmpty()) return set;
        for (String d : workDays.split(",")) {
            d = d.trim().toUpperCase(Locale.ROOT);
            switch (d) {
                case "L": set.add(DayOfWeek.MONDAY); break;
                case "M": set.add(DayOfWeek.TUESDAY); break;
                case "X": set.add(DayOfWeek.WEDNESDAY); break;
                case "J": set.add(DayOfWeek.THURSDAY); break;
                case "V": set.add(DayOfWeek.FRIDAY); break;
                case "S": set.add(DayOfWeek.SATURDAY); break;
                case "D": set.add(DayOfWeek.SUNDAY); break;
            }
        }
        return set;
    }

    private ScheduleConflictDto buildConflict(MedicalAgenda cand, MedicalAgenda ex) {
        return new ScheduleConflictDto(
                ex.getId(),
                "OVERLAP",
                "Conflicto con otra agenda",
                ex.getId(),
                null,
                cand.getStartDate() != null ? cand.getStartDate().format(ISO_DATE) : null,
                cand.getStartTime(),
                cand.getEndTime()
        );
    }
    private String computeStatus(LocalDate start, LocalDate end, boolean isActive) {
        try {
            LocalDate today = LocalDate.now();
            if (!isActive) return "INACTIVA";
            if (end.isBefore(today)) return "VENCIDA";
            if (!start.isAfter(today) && !end.isBefore(today)) {
                if (end.minusDays(7).isBefore(today)) return "PROXIMA_A_VENCER";
                return "VIGENTE";
            }
            return "VIGENTE";
        } catch (Exception e) {
            return "VIGENTE";
        }
    }

    private Mono<MedicalAgendaView> enrichWithEmployee(MedicalAgenda ma, HttpHeaders headers) {
        String auth = headers.getFirst("Authorization");
        String empId = headers.getFirst("x-employee-id");

        // Si no hay cabeceras de autenticación, devolver datos mínimos sin consultar empleado
        if (auth == null && empId == null) {
            MedicalAgendaView.EmployeeDto e = new MedicalAgendaView.EmployeeDto(ma.getEmployeeId(), "", "", "", "", 0, true);
            MedicalAgendaView view = new MedicalAgendaView(
                    ma.getId(),
                    ma.getEmployeeId(),
                    e,
                    ma.getModality(),
                    ma.getHeadquartersId(),
                    ma.getOfficeId(),
                    ma.getStartDate() != null ? ma.getStartDate().format(ISO_DATE) : null,
                    ma.getEndDate() != null ? ma.getEndDate().format(ISO_DATE) : null,
                    ma.getWorkDays(),
                    ma.getStartTime(),
                    ma.getEndTime(),
                    ma.getAppointmentDuration(),
                    ma.getServiceTypes(),
                    ma.getAllowGroupSession(),
                    ma.getRequiresReferral(),
                    ma.getNotes(),
                    ma.getStatus(),
                    ma.getIsActive(),
                    ma.getCreatedAt() != null ? ma.getCreatedAt().toString() : null,
                    ma.getUpdatedAt() != null ? ma.getUpdatedAt().toString() : null
            );
            return Mono.just(view);
        }

        return webClient.get()
                .uri(gatewayUrl + "/api/v1/employees/" + ma.getEmployeeId())
                .headers(h -> {
                    if (auth != null) h.add("Authorization", auth);
                    if (empId != null) h.add("x-employee-id", empId);
                })
                .retrieve()
                .bodyToMono(EmployeeMini.class)
                .onErrorResume(ex -> Mono.just(new EmployeeMini(ma.getEmployeeId(), "", "", "", "", 0, true)))
                .map(emp -> {
                    MedicalAgendaView.EmployeeDto e = new MedicalAgendaView.EmployeeDto(emp.id, emp.names, emp.lastnames, emp.identification_type, emp.identification_number, emp.rol_id, emp.is_active);
                    MedicalAgendaView view = new MedicalAgendaView(
                            ma.getId(),
                            ma.getEmployeeId(),
                            e,
                            ma.getModality(),
                            ma.getHeadquartersId(),
                            ma.getOfficeId(),
                            ma.getStartDate() != null ? ma.getStartDate().format(ISO_DATE) : null,
                            ma.getEndDate() != null ? ma.getEndDate().format(ISO_DATE) : null,
                            ma.getWorkDays(),
                            ma.getStartTime(),
                            ma.getEndTime(),
                            ma.getAppointmentDuration(),
                            ma.getServiceTypes(),
                            ma.getAllowGroupSession(),
                            ma.getRequiresReferral(),
                            ma.getNotes(),
                            ma.getStatus(),
                            ma.getIsActive(),
                            ma.getCreatedAt() != null ? ma.getCreatedAt().toString() : null,
                            ma.getUpdatedAt() != null ? ma.getUpdatedAt().toString() : null
                    );
                    return view;
                });
    }

    static class EmployeeMini {
        public Long id;
        public String names;
        public String lastnames;
        public String identification_type;
        public String identification_number;
        public Integer rol_id;
        public Boolean is_active;

        public EmployeeMini() {}
        public EmployeeMini(Long id, String names, String lastnames, String identification_type, String identification_number, Integer rol_id, Boolean is_active) {
            this.id = id; this.names = names; this.lastnames = lastnames; this.identification_type = identification_type; this.identification_number = identification_number; this.rol_id = rol_id; this.is_active = is_active;
        }
    }
}
