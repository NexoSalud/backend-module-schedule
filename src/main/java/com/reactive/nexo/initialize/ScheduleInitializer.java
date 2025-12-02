package com.reactive.nexo.initialize;

import com.reactive.nexo.model.Schedule;
import com.reactive.nexo.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class ScheduleInitializer implements CommandLineRunner {
    
    private final ScheduleRepository scheduleRepository;
    
    @Override
    public void run(String... args) throws Exception {
        scheduleRepository.count()
                .flatMapMany(count -> {
                    if (count == 0) {
                        log.info("Initializing schedule data...");
                        return createSampleSchedules();
                    } else {
                        log.info("Schedule data already exists, skipping initialization");
                        return Flux.empty();
                    }
                })
                .doOnComplete(() -> log.info("Schedule initialization completed"))
                .subscribe();
    }
    
    private Flux<Schedule> createSampleSchedules() {
        List<Schedule> schedules = new ArrayList<>();
        LocalDateTime baseTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0).withNano(0);
        
        // Crear citas para empleados 1-7
        for (long employeeId = 1; employeeId <= 7; employeeId++) {
            // Cita matutina
            LocalDateTime morningStart = baseTime.plusDays(employeeId - 1).withHour(9);
            LocalDateTime morningEnd = morningStart.plusHours(1);
            schedules.add(new Schedule(
                employeeId,
                employeeId, // Usando el mismo ID para user por simplicidad
                morningStart,
                morningEnd,
                String.format("Consulta general - Empleado %d", employeeId),
                false // Sesión individual
            ));
            
            // Cita vespertina
            LocalDateTime afternoonStart = baseTime.plusDays(employeeId - 1).withHour(14);
            LocalDateTime afternoonEnd = afternoonStart.plusHours(1).plusMinutes(30);
            schedules.add(new Schedule(
                employeeId,
                employeeId + 10, // Usuario diferente
                afternoonStart,
                afternoonEnd,
                String.format("Control médico - Empleado %d", employeeId),
                false // Sesión individual
            ));
            
            // Cita adicional para algunos empleados
            if (employeeId <= 4) {
                LocalDateTime extraStart = baseTime.plusDays(employeeId - 1).withHour(16);
                LocalDateTime extraEnd = extraStart.plusMinutes(45);
                schedules.add(new Schedule(
                    employeeId,
                    employeeId + 20, // Usuario diferente
                    extraStart,
                    extraEnd,
                    String.format("Procedimiento especializado - Empleado %d", employeeId),
                    false // Sesión individual
                ));
            }
        }
        
        // Agregar algunas citas para la próxima semana
        LocalDateTime nextWeek = baseTime.plusDays(7);
        for (long employeeId = 1; employeeId <= 5; employeeId++) {
            LocalDateTime start = nextWeek.plusDays(employeeId - 1).withHour(10);
            LocalDateTime end = start.plusHours(2);
            schedules.add(new Schedule(
                employeeId,
                employeeId + 30,
                start,
                end,
                String.format("Cita de seguimiento semanal - Empleado %d", employeeId),
                false // Sesión individual
            ));
        }
        
        // Agregar sesiones grupales de ejemplo
        // Empleado 1: Sesión grupal de terapia
        LocalDateTime groupSession1Start = baseTime.plusDays(2).withHour(16);
        LocalDateTime groupSession1End = groupSession1Start.plusHours(1);
        
        schedules.add(new Schedule(1L, 100L, groupSession1Start, groupSession1End, 
                "Terapia grupal - Manejo del estrés", true));
        schedules.add(new Schedule(1L, 101L, groupSession1Start, groupSession1End, 
                "Terapia grupal - Manejo del estrés", true));
        schedules.add(new Schedule(1L, 102L, groupSession1Start, groupSession1End, 
                "Terapia grupal - Manejo del estrés", true));
        
        // Empleado 2: Sesión grupal educativa
        LocalDateTime groupSession2Start = baseTime.plusDays(3).withHour(11);
        LocalDateTime groupSession2End = groupSession2Start.plusHours(1).plusMinutes(30);
        
        schedules.add(new Schedule(2L, 103L, groupSession2Start, groupSession2End, 
                "Sesión educativa - Nutrición saludable", true));
        schedules.add(new Schedule(2L, 104L, groupSession2Start, groupSession2End, 
                "Sesión educativa - Nutrición saludable", true));
        
        // Empleado 3: Sesión grupal de ejercicios
        LocalDateTime groupSession3Start = baseTime.plusDays(5).withHour(8);
        LocalDateTime groupSession3End = groupSession3Start.plusMinutes(45);
        
        schedules.add(new Schedule(3L, 105L, groupSession3Start, groupSession3End, 
                "Clase grupal de ejercicios terapéuticos", true));
        schedules.add(new Schedule(3L, 106L, groupSession3Start, groupSession3End, 
                "Clase grupal de ejercicios terapéuticos", true));
        schedules.add(new Schedule(3L, 107L, groupSession3Start, groupSession3End, 
                "Clase grupal de ejercicios terapéuticos", true));
        schedules.add(new Schedule(3L, 108L, groupSession3Start, groupSession3End, 
                "Clase grupal de ejercicios terapéuticos", true));
        
        return scheduleRepository.saveAll(schedules);
    }
}