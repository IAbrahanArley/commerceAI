package br.com.commerceai.api.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChatRequest(
        @NotBlank(message = "sessionId e obrigatorio")
        @Size(max = 64, message = "sessionId deve ter no maximo 64 caracteres")
        @Pattern(regexp = "[a-zA-Z0-9_-]+", message = "sessionId deve conter apenas letras, numeros, _ ou -")
        String sessionId,
        @NotBlank(message = "message e obrigatoria")
        @Size(max = 4000, message = "message deve ter no maximo 4000 caracteres")
        String message) {
}
