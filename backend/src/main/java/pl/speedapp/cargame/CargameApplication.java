package pl.speedapp.cargame;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import pl.speedapp.cargame.conditions.CorsDisabled;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;

import static springfox.documentation.builders.PathSelectors.regex;

@SpringBootApplication
@EnableJpaAuditing
@EnableAutoConfiguration
@EnableSwagger2
public class CargameApplication {

    public static void main(String[] args) {
        SpringApplication.run(CargameApplication.class, args);
    }

    @Bean
    @Conditional(CorsDisabled.class)
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedMethods("HEAD", "GET", "PUT", "POST", "DELETE", "PATCH")
                        .allowedOrigins("http://localhost:8080");
            }
        };
    }

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .paths(regex("/api/.*"))
                .build()
                .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        return new ApiInfo(
                "CarGame REST API",
                "SpeedApp recruitment task.",
                "API v1",
                "Terms of service",
                new Contact("SpeedApp", "http://speedapp.pl/", "speedapp@speedapp.pl\n"),
                "", "", Collections.emptyList());
    }
}
