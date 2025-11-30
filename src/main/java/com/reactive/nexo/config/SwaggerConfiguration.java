package com.reactive.nexo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfiguration {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Nexo Salud Schedule API")
                .version("v1.0")
                .description("API para gestión de citas y horarios en Nexo Salud. " +
                           "Este módulo permite crear, consultar, actualizar y eliminar citas, " +
                           "con validación automática de solapamiento de horarios para empleados y usuarios.")
                .contact(new Contact()
                    .name("Equipo de Desarrollo Nexo Salud")
                    .email("desarrollo@nexosalud.com")
                    .url("https://nexosalud.com"))
                .license(new License()
                    .name("Proprietary")
                    .url("https://nexosalud.com/license")))
            .addServersItem(new Server()
                .url("http://localhost:8083")
                .description("Servidor de desarrollo local"))
            .addServersItem(new Server()
                .url("http://local.nexosalud:8083")
                .description("Servidor de desarrollo local (dominio personalizado)"));
    }
}