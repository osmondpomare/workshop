package com.nequi.bco.wks901.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Programmatic OpenAPI 3.0 specification for the reversal services API.
 */
@Configuration
public class OpenApiSpecConfig {

    @Bean
    public OpenAPI reversalOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Workshop Reversal Services (WKS901)")
                        .description("Reactive reversal (backout) services: register, query and "
                                + "asynchronously confirm transaction reversals with FX conversion.")
                        .version("1.0.0")
                        .contact(new Contact().name("Nequi").email("soporte@nequi.com.co"))
                        .license(new License().name("Proprietary")));
    }
}
