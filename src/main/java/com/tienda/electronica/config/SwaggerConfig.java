package com.tienda.electronica.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Tienda Electrónica - API REST")
                        .description("API REST para la gestión completa de una tienda electrónica. " +
                                "Incluye gestión de clientes, catálogo de productos electrónicos y procesamiento de pedidos. "
                                +
                                "Desarrollado como parte de la Fase 1 de evaluación de microservicios " +
                                "aplicando técnicas de aseguramiento de calidad de software según ISO/IEC 25010.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipo de Desarrollo - Tienda Electrónica")
                                .email("desarrollo@tiendaelectronica.com")
                                .url("https://github.com/tienda-electronica/microservicio"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
