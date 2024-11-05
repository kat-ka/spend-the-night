package com.github.kat_ka.spend_the_night.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

	@Bean
	protected OpenAPI customOpenApi() {
		return new OpenAPI()
				.components(new Components()
						.addSecuritySchemes("bearer-key",
								new SecurityScheme()
									.type(SecurityScheme.Type.HTTP)
									.scheme("bearer")
									.bearerFormat("JWT")
						)
				)
				.addSecurityItem(new SecurityRequirement().addList("bearer-key"))
				.info(new Info()
						.title("spend-the-night API")
						.version("1.0.0")
						.description("Managing and discovering accommodation offers")
						.contact(new Contact().url("https://github.com/kat-ka"))
						.license(new License().name("Apache 2.0").url("http://www.apache.org/licenses/LICENSE-2.0"))
				);
	}
}
