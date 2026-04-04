package com.finSecure;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.Executors;

@OpenAPIDefinition(
		info = @Info(
				title = "finSecure API",
				version = "1.0",
				description = "Finance dashboard backend"
		)
)
@SecurityScheme(
		name = "bearerAuth",
		type = SecuritySchemeType.HTTP,
		scheme = "bearer",
		bearerFormat = "JWT"
)
@SpringBootApplication
public class FinsecureApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinsecureApplication.class, args);
	}

	@Bean
	public TomcatProtocolHandlerCustomizer<?> virtualThreadsForTomcat() {
		return protocolHandler ->
				protocolHandler.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
	}

}
