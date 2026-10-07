package de.rpg.game.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RPG Game Backend API")
                        .version("1.0.0")
                        .description("REST API für das Java Spring Boot RPG Spiel. " +
                                "Ermöglicht die Verwaltung von Spielern, Monstern, Inventar und Kampfsystem."));
    }
}
