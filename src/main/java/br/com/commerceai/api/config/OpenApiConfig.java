package br.com.commerceai.api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@OpenAPIDefinition(info = @Info(
        title = "Commerce AI API",
        version = "0.0.1-SNAPSHOT",
        description = "API de assistente comercial. Credenciais do Gemini sao configuradas somente no backend."))
public class OpenApiConfig {
}
