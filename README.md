# reactive-nexo-schedule — Backend [Schedule Module]

Módulo de citas/schedule para el proyecto NEXO-Hospital [API reactiva con Spring WebFlux y R2DBC].

Estado actual del proyecto
- Paquete raíz de la aplicación: `com.reactive.nexo`.
- Stack: Spring Boot 3.x, Spring WebFlux, Spring Data R2DBC.
- Bases de datos: soporta R2DBC (Postgres u otros drivers). Para pruebas y desarrollo ligero el proyecto usa H2 en memoria (configurable en `application.yml`).

Requisitos mínimos
- Java 17 o superior (en este repositorio se ha usado Java 19 en pruebas).
- Maven 3.6+

El proyecto ya incluye springdoc (starter WebFlux UI), por lo que la documentación OpenAPI/Swagger queda disponible en las rutas por defecto. 
En tu entorno local (ejecutando la app en el puerto por defecto 8083) puedes encontrarla en:

Interfaz web (Swagger UI):
http://localhost:8083/swagger-ui.html

Documento OpenAPI (JSON):
http://localhost:8083/v3/api-docs

## Modelo de datos

El módulo schedule maneja citas entre empleados y usuarios con la siguiente estructura:

```sql
schedule:
- id: Identificador único de la cita
- employee_id: ID del empleado asignado
- user_id: ID del usuario/paciente
- start_at: Fecha y hora de inicio de la cita
- end_at: Fecha y hora de fin de la cita
- details: Detalles adicionales de la cita
- created_at: Fecha de creación del registro
- updated_at: Fecha de última actualización
```

## Validaciones de negocio

- No se pueden crear citas que se solapen para un mismo employee_id
- No se pueden crear citas que se solapen para un mismo user_id
- Las citas deben tener fecha de fin posterior a fecha de inicio

## Endpoints principales

- GET /api/v1/schedule — lista todas las citas
- POST /api/v1/schedule — crea una nueva cita
- GET /api/v1/schedule/{id} — obtiene una cita específica
- PUT /api/v1/schedule/{id} — actualiza una cita
- DELETE /api/v1/schedule/{id} — elimina una cita
- GET /api/v1/schedule/employee/{employeeId} — obtiene todas las citas de un empleado
- GET /api/v1/schedule/user/{userId} — obtiene todas las citas de un usuario

## Datos de ejemplo

El módulo incluye datos de ejemplo para employees con IDs del 1 al 7, con citas distribuidas en diferentes horarios.

## Cómo compilar y ejecutar

1. Compilar y ejecutar tests:
```bash
mvn clean test
```

2. Empaquetar la aplicación:
```bash
mvn clean package
```

3. Ejecutar la JAR producida:
```bash
java -jar target/reactive-nexo-schedule-0.0.1-SNAPSHOT.jar
```