package com.lubj9.urlshortener.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI()
                .info(new Info()
                        .title("URL Shortener API")
                        .description("Encurtador de URLs com cache, Base62 encoding e estatísticas de acesso.")
                        .version("0.1.0")
                        .contact(new Contact()
                                .name("Lucas Zeferino Baracat")
                                .email("lucasbaracatprofissional@gmail.com")
                                .url("https://github.com/lubj9")));
    }
}
