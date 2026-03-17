package com.reactive.nexo.service;

import com.reactive.nexo.dto.MedicalAgendaView;
import com.reactive.nexo.dto.CalendarEventDto;
import com.reactive.nexo.dto.ScheduleConflictDto;
import com.reactive.nexo.dto.PagedResponse;
import com.reactive.nexo.dto.CreateMedicalAgendaRequest;
import com.reactive.nexo.model.MedicalAgenda;
import com.reactive.nexo.repository.MedicalAgendaRepository;
import com.reactive.nexo.repository.CalendarExceptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MedicalAgendaService {

    private final MedicalAgendaRepository repository;
    private final CalendarExceptionRepository exceptionRepository;

    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter ISO_TIME = DateTimeFormatter.ofPattern("HH:mm");

    public Mono<PagedResponse<MedicalAgendaView>> listAgendas(Integer page, Integer size, Long employeeId,
            HttpHeaders headers) {
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
                .flatMap(ma -> enrichWithEmployee(ma, null, headers))
                .collectList()
                .map(list -> new PagedResponse<>(list, p, s, total, (total + s - 1) / s,
                        p >= ((total + s - 1) / s) - 1)));
    }

    public Mono<MedicalAgendaView> getById(Long id, HttpHeaders headers) {
        return repository.findById(id)
                .flatMap(ma -> checkConflicts(ma).flatMap(conflicts -> enrichWithEmployee(ma, conflicts, headers)));
    }

    public Mono<MedicalAgenda> create(MedicalAgenda payload) {
        payload.setStatus(computeStatus(payload.getStartDate(), payload.getEndDate(),
                Boolean.TRUE.equals(payload.getIsActive())));
        payload.setCreatedAt(LocalDateTime.now());
        payload.setUpdatedAt(LocalDateTime.now());

        return checkConflicts(payload).flatMap(conflicts -> {
            if (conflicts != null && !conflicts.isEmpty()) {
                payload.setEnabled(false);
            } else if (payload.getEnabled() == null) {
                payload.setEnabled(true);
            }
            return repository.save(payload);
        });
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
                ? dto.getServiceTypeIds().stream().map(String::valueOf)
                        .collect(java.util.stream.Collectors.joining(","))
                : null;
        payload.setServiceTypes(serviceTypesCsv);
        payload.setAllowGroupSession(Boolean.TRUE.equals(dto.getAllowGroupSession()));
        payload.setRequiresReferral(Boolean.TRUE.equals(dto.getRequiresReferral()));
        payload.setNotes(dto.getNotes());

        // Novedades Phase 1
        String conveniosCsv = (dto.getConvenioIds() != null && !dto.getConvenioIds().isEmpty())
                ? dto.getConvenioIds().stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(","))
                : null;
        payload.setConvenios(conveniosCsv);
        payload.setCanCreateMedicalHistory(Boolean.TRUE.equals(dto.getCanCreateMedicalHistory()));
        payload.setDoubleShift(Boolean.TRUE.equals(dto.getDoubleShift()));

        // Novedades Phase 2
        payload.setEnabledSlots(dto.getEnabledSlots());

        // Novedades Phase 3
        payload.setAgendaState(dto.getAgendaState() != null ? dto.getAgendaState() : "ABIERTA");
        payload.setCreatedBy(dto.getCreatedBy());

        // Phase 5
        payload.setFrequency(dto.getFrequency());

        payload.setIsActive(true); // por defecto activa

        payload.setStatus(computeStatus(payload.getStartDate(), payload.getEndDate(),
                Boolean.TRUE.equals(payload.getIsActive())));
        payload.setCreatedAt(LocalDateTime.now());
        payload.setUpdatedAt(LocalDateTime.now());

        return checkConflicts(payload).flatMap(conflicts -> {
            if (conflicts != null && !conflicts.isEmpty()) {
                payload.setEnabled(false);
            } else {
                payload.setEnabled(true);
            }
            return repository.save(payload);
        });
    }

    public Mono<List<MedicalAgenda>> cloneAgenda(Long id, String frequency) {
        return repository.findById(id)
                .flatMap(original -> {
                    original.setEnabled(false);
                    return repository.save(original).thenReturn(original);
                })
                .flatMap(original -> {
                    int clonesToCreate = 1; // Default
                    if ("1".equals(frequency))
                        clonesToCreate = 2; // Original + 1
                    else if ("2".equals(frequency))
                        clonesToCreate = 3; // Original + 2
                    else if ("3".equals(frequency))
                        clonesToCreate = 4; // Original + 3
                    else if ("TODOS".equalsIgnoreCase(frequency) || "ALL".equalsIgnoreCase(frequency))
                        clonesToCreate = 12; // Original + 11

                    List<MedicalAgenda> newClones = new ArrayList<>();
                    for (int i = 0; i < clonesToCreate; i++) {
                        MedicalAgenda clone = new MedicalAgenda();
                        clone.setEmployeeId(original.getEmployeeId());
                        clone.setModality(original.getModality());
                        clone.setHeadquartersId(original.getHeadquartersId());
                        clone.setOfficeId(original.getOfficeId());
                        clone.setStartDate(original.getStartDate().plusMonths(i));
                        clone.setEndDate(original.getEndDate().plusMonths(i));
                        clone.setWorkDays(original.getWorkDays());
                        clone.setStartTime(original.getStartTime());
                        clone.setEndTime(original.getEndTime());
                        clone.setAppointmentDuration(original.getAppointmentDuration());
                        clone.setServiceTypes(original.getServiceTypes());
                        clone.setAllowGroupSession(original.getAllowGroupSession());
                        clone.setRequiresReferral(original.getRequiresReferral());
                        clone.setNotes(
                                original.getNotes() != null ? "Copia de: " + original.getNotes() : "Copia de agenda");

                        clone.setConvenios(original.getConvenios());
                        clone.setCanCreateMedicalHistory(original.getCanCreateMedicalHistory());
                        clone.setDoubleShift(original.getDoubleShift());
                        clone.setEnabledSlots(original.getEnabledSlots());

                        clone.setAgendaState("ABIERTA");
                        clone.setCreatedBy(original.getCreatedBy());
                        clone.setFrequency(frequency);
                        clone.setIsActive(true);
                        clone.setEnabled(true);
                        clone.setStatus(computeStatus(clone.getStartDate(), clone.getEndDate(), true));
                        clone.setCreatedAt(LocalDateTime.now());
                        clone.setUpdatedAt(LocalDateTime.now());

                        newClones.add(clone);
                    }

                    // Check conflicts and save for each
                    return Flux.fromIterable(newClones)
                            .flatMap(clone -> checkConflicts(clone)
                                    .flatMap(conflicts -> {
                                        if (conflicts != null && !conflicts.isEmpty()) {
                                            clone.setEnabled(false);
                                        }
                                        return repository.save(clone);
                                    }))
                            .collectList();
                });
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

                    // Novedades Phase 1
                    existing.setConvenios(payload.getConvenios());
                    existing.setCanCreateMedicalHistory(payload.getCanCreateMedicalHistory());
                    existing.setDoubleShift(payload.getDoubleShift());

                    // Novedades Phase 2
                    existing.setEnabledSlots(payload.getEnabledSlots());

                    // Novedades Phase 3
                    existing.setAgendaState(payload.getAgendaState());
                    existing.setCreatedBy(payload.getCreatedBy());

                    // Phase 5
                    existing.setFrequency(payload.getFrequency());

                    existing.setIsActive(payload.getIsActive());
                    existing.setEnabled(payload.getEnabled());
                    existing.setStatus(computeStatus(existing.getStartDate(), existing.getEndDate(),
                            Boolean.TRUE.equals(existing.getIsActive())));
                    existing.setUpdatedAt(LocalDateTime.now());
                    return repository.save(existing);
                });
    }

    public Mono<MedicalAgenda> patch(Long id, MedicalAgenda payload) {
        return repository.findById(id)
                .flatMap(existing -> {
                    if (payload.getEmployeeId() != null)
                        existing.setEmployeeId(payload.getEmployeeId());
                    if (payload.getModality() != null)
                        existing.setModality(payload.getModality());
                    if (payload.getHeadquartersId() != null)
                        existing.setHeadquartersId(payload.getHeadquartersId());
                    if (payload.getOfficeId() != null)
                        existing.setOfficeId(payload.getOfficeId());
                    if (payload.getStartDate() != null)
                        existing.setStartDate(payload.getStartDate());
                    if (payload.getEndDate() != null)
                        existing.setEndDate(payload.getEndDate());
                    if (payload.getWorkDays() != null)
                        existing.setWorkDays(payload.getWorkDays());
                    if (payload.getStartTime() != null)
                        existing.setStartTime(payload.getStartTime());
                    if (payload.getEndTime() != null)
                        existing.setEndTime(payload.getEndTime());
                    if (payload.getAppointmentDuration() != null)
                        existing.setAppointmentDuration(payload.getAppointmentDuration());
                    if (payload.getServiceTypes() != null)
                        existing.setServiceTypes(payload.getServiceTypes());
                    if (payload.getAllowGroupSession() != null)
                        existing.setAllowGroupSession(payload.getAllowGroupSession());
                    if (payload.getRequiresReferral() != null)
                        existing.setRequiresReferral(payload.getRequiresReferral());
                    if (payload.getNotes() != null)
                        existing.setNotes(payload.getNotes());

                    // Novedades Phase 1
                    if (payload.getConvenios() != null)
                        existing.setConvenios(payload.getConvenios());
                    if (payload.getCanCreateMedicalHistory() != null)
                        existing.setCanCreateMedicalHistory(payload.getCanCreateMedicalHistory());
                    if (payload.getDoubleShift() != null)
                        existing.setDoubleShift(payload.getDoubleShift());

                    // Novedades Phase 2
                    if (payload.getEnabledSlots() != null)
                        existing.setEnabledSlots(payload.getEnabledSlots());

                    // Novedades Phase 3
                    if (payload.getAgendaState() != null)
                        existing.setAgendaState(payload.getAgendaState());
                    if (payload.getCreatedBy() != null)
                        existing.setCreatedBy(payload.getCreatedBy());

                    if (payload.getFrequency() != null)
                        existing.setFrequency(payload.getFrequency());

                    if (payload.getIsActive() != null)
                        existing.setIsActive(payload.getIsActive());
                    if (payload.getEnabled() != null)
                        existing.setEnabled(payload.getEnabled());
                    existing.setStatus(computeStatus(existing.getStartDate(), existing.getEndDate(),
                            Boolean.TRUE.equals(existing.getIsActive())));
                    existing.setUpdatedAt(LocalDateTime.now());
                    return repository.save(existing);
                });
    }

    public Mono<MedicalAgenda> updateEnabled(Long id, Boolean enabled) {
        return repository.findById(id)
                .flatMap(existing -> {
                    existing.setEnabled(enabled);
                    existing.setUpdatedAt(LocalDateTime.now());
                    return repository.save(existing);
                });
    }

    private Mono<Void> validateLocation(CreateMedicalAgendaRequest dto) {
        if ("intramural".equals(dto.getModality())) {
            if (dto.getHeadquartersId() == null || dto.getOfficeId() == null) {
                return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Para modalidad intramural se requiere Sede y Consultorio"));
            }
        }
        return Mono.empty();
    }

    // Novedad Phase 2: Validación de conflictos de consultorio
    public Mono<Boolean> checkOfficeConflict(Long officeId, String startDateStr, String endDateStr,
            String startTimeStr, String endTimeStr, String workDaysCsv,
            Long excludeAgendaId) {
        if (officeId == null)
            return Mono.just(false);

        LocalDate startDate = LocalDate.parse(startDateStr, ISO_DATE);
        LocalDate endDate = LocalDate.parse(endDateStr, ISO_DATE);
        LocalTime startTime = LocalTime.parse(startTimeStr, ISO_TIME);
        LocalTime endTime = LocalTime.parse(endTimeStr, ISO_TIME);
        List<String> workDays = Arrays.asList(workDaysCsv.split(","));

        return repository.findByOfficeIdAndDateRange(officeId, startDate, endDate)
                .filter(agenda -> excludeAgendaId == null || !agenda.getId().equals(excludeAgendaId))
                .filter(agenda -> {
                    // Verificamos cruce de días de la semana
                    if (agenda.getWorkDays() == null)
                        return false;
                    List<String> existingDays = Arrays.asList(agenda.getWorkDays().split(","));
                    boolean hasDayOverlap = existingDays.stream().anyMatch(workDays::contains);
                    if (!hasDayOverlap)
                        return false;

                    // Verificamos cruce de horas
                    LocalTime existingStart = LocalTime.parse(agenda.getStartTime(), ISO_TIME);
                    LocalTime existingEnd = LocalTime.parse(agenda.getEndTime(), ISO_TIME);

                    return startTime.isBefore(existingEnd) && endTime.isAfter(existingStart);
                })
                .hasElements();
    }

    public Mono<Void> delete(Long id) {
        return repository.deleteById(id);
    }

    public Mono<List<CalendarEventDto>> getCalendarEvents(Long employeeId, String startDate, String endDate) {
        LocalDate start = LocalDate.parse(startDate, ISO_DATE);
        LocalDate end = LocalDate.parse(endDate, ISO_DATE);
        return repository.findByEmployeeId(employeeId)
                .filter(ma -> Boolean.TRUE.equals(ma.getEnabled())
                        && isDateRangeOverlap(start, end, ma.getStartDate(), ma.getEndDate()))
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
                                        null));
                            }
                        }
                    }
                    return events;
                })
                .flatMap(events -> exceptionRepository.findByEmployeeIdAndDateRange(employeeId, start, end)
                        .map(ex -> new CalendarEventDto(
                                -ex.getId(),
                                ex.getExceptionDate().format(ISO_DATE),
                                "NO_LABORABLE",
                                ex.getReason() != null ? ex.getReason() : "Excepción",
                                ex.getStartTime() != null ? ex.getStartTime() : "00:00",
                                ex.getEndTime() != null ? ex.getEndTime() : "23:59",
                                null))
                        .collectList()
                        .map(exceptions -> {
                            events.addAll(exceptions);
                            return events;
                        }));
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
        if (!isDateRangeOverlap(aStart, aEnd, bStart, bEnd))
            return false;

        // Intersección de días de la semana
        Set<DayOfWeek> aDays = parseWorkDays(a.getWorkDays());
        Set<DayOfWeek> bDays = parseWorkDays(b.getWorkDays());
        boolean dayOverlap = aDays.stream().anyMatch(bDays::contains);
        if (!dayOverlap)
            return false;

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
        if (workDays == null || workDays.isEmpty())
            return set;
        for (String d : workDays.split(",")) {
            d = d.trim().toUpperCase(Locale.ROOT);
            switch (d) {
                case "L":
                    set.add(DayOfWeek.MONDAY);
                    break;
                case "M":
                    set.add(DayOfWeek.TUESDAY);
                    break;
                case "X":
                    set.add(DayOfWeek.WEDNESDAY);
                    break;
                case "J":
                    set.add(DayOfWeek.THURSDAY);
                    break;
                case "V":
                    set.add(DayOfWeek.FRIDAY);
                    break;
                case "S":
                    set.add(DayOfWeek.SATURDAY);
                    break;
                case "D":
                    set.add(DayOfWeek.SUNDAY);
                    break;
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
                cand.getEndTime());
    }

    private String computeStatus(LocalDate start, LocalDate end, boolean isActive) {
        try {
            LocalDate today = LocalDate.now();
            if (!isActive)
                return "INACTIVA";
            if (end.isBefore(today))
                return "VENCIDA";
            if (!start.isAfter(today) && !end.isBefore(today)) {
                if (end.minusDays(7).isBefore(today))
                    return "PROXIMA_A_VENCER";
                return "VIGENTE";
            }
            return "VIGENTE";
        } catch (Exception e) {
            return "VIGENTE";
        }
    }

    private Mono<MedicalAgendaView> enrichWithEmployee(MedicalAgenda ma, List<ScheduleConflictDto> conflicts,
            HttpHeaders headers) {
        MedicalAgendaView view = new MedicalAgendaView(
                ma.getId(),
                ma.getEmployeeId(),
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
                ma.getEnabled(),
                ma.getConvenios(), // Phase 1
                ma.getCanCreateMedicalHistory(), // Phase 1
                ma.getDoubleShift(), // Phase 1
                ma.getEnabledSlots(), // Phase 2
                ma.getAgendaState(), // Phase 3
                ma.getFrequency(), // Phase 5
                ma.getCreatedBy(), // Phase 3
                ma.getCreatedAt() != null ? ma.getCreatedAt().toString() : null,
                ma.getUpdatedAt() != null ? ma.getUpdatedAt().toString() : null,
                conflicts != null ? conflicts : java.util.Collections.emptyList());
        return Mono.just(view);
    }

    // Phase 6: Export to CSV
    public Mono<byte[]> exportAgendasCsv(Long employeeId, HttpHeaders headers) {
        Flux<MedicalAgenda> src = (employeeId != null)
                ? repository.findByEmployeeIdOrdered(employeeId)
                : repository.findAllOrdered();

        return src.flatMap(ma -> enrichWithEmployee(ma, null, headers))
                .collectList()
                .map(this::generateCsvContent);
    }

    private byte[] generateCsvContent(List<MedicalAgendaView> list) {
        StringBuilder csv = new StringBuilder();
        // UTF-8 BOM for Excel
        csv.append('\uFEFF');
        csv.append(
                "ID,Profesional ID,Modalidad,Sede ID,Consultorio ID,Fecha Inicio,Fecha Fin,Dias,Horario,Cita(min),Estado Agenda,Estado,Creado En\n");
        for (MedicalAgendaView view : list) {
            csv.append(view.getId() != null ? view.getId() : "").append(",");
            csv.append(view.getEmployeeId() != null ? view.getEmployeeId() : "").append(",");
            csv.append(escapeCsv(view.getModality())).append(",");
            csv.append(view.getHeadquartersId() != null ? view.getHeadquartersId() : "").append(",");
            csv.append(view.getOfficeId() != null ? view.getOfficeId() : "").append(",");
            csv.append(escapeCsv(view.getStartDate())).append(",");
            csv.append(escapeCsv(view.getEndDate())).append(",");
            csv.append(escapeCsv(view.getWorkDays())).append(",");
            csv.append(escapeCsv(view.getStartTime() + " - " + view.getEndTime())).append(",");
            csv.append(view.getAppointmentDuration() != null ? view.getAppointmentDuration() : "").append(",");
            csv.append(escapeCsv(view.getAgendaState())).append(",");
            csv.append(escapeCsv(view.getStatus())).append(",");
            csv.append(escapeCsv(view.getCreatedAt())).append("\n");
        }
        return csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private String escapeCsv(String data) {
        if (data == null)
            return "";
        String escaped = data;
        if (escaped.contains("\"")) {
            escaped = escaped.replace("\"", "\"\"");
        }
        if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\"")) {
            escaped = "\"" + escaped + "\"";
        }
        return escaped;
    }
}
