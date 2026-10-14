package br.com.commerceai.api.chat.service;

import org.springframework.http.HttpStatus;

public class ChatProviderException extends RuntimeException {

    private final HttpStatus status;

    public ChatProviderException(HttpStatus status) {
        super("Nao foi possivel obter uma resposta do assistente.");
        this.status = status;
    }

    public HttpStatus status() {
        return status;
    }
}
