package com.gdamiens.website.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApi30Config {

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
      .info(new Info().title("Guillaume Damiens API")
        .description("API of Guillaume Damiens website components & projets")
        .version("v1.0.0")
        .license(new License().name("Apache 2.0")))
      .components(new Components()
        .addSecuritySchemes("Auth. Token",
          new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")));
  }
}
