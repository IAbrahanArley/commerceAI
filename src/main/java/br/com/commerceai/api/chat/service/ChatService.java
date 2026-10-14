package br.com.commerceai.api.chat.service;

import br.com.commerceai.api.chat.dto.ChatRequest;
import br.com.commerceai.api.chat.dto.ChatResponse;
import com.google.genai.errors.ApiException;
import java.io.IOException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private static final String SYSTEM_PROMPT = """
            Voce e o Commerce AI, um assistente comercial para lojas fisicas e e-commerce.
            Responda sempre em portugues brasileiro, de forma clara e objetiva.
            Ajude o cliente a esclarecer o que procura, perguntando sobre necessidades e orcamento.
            Nesta versao voce nao tem acesso a catalogo, precos, estoque ou politicas da loja.
            Nunca invente produtos, precos, disponibilidade, links ou politicas comerciais.
            Informe essa limitacao quando uma resposta depender desses dados.
            Nao afirme ter consultado sistemas ou realizado operacoes.
            """;

    private final ChatClient chatClient;

    public ChatService(ChatClient.Builder builder) {
        this.chatClient = builder.defaultSystem(SYSTEM_PROMPT).build();
    }

    public ChatResponse chat(ChatRequest request) {
        String response;
        try {
            response = chatClient.prompt().user(request.message()).call().content();
        } catch (RuntimeException exception) {
            throw translateProviderFailure(exception);
        }
        if (response == null || response.isBlank()) {
            throw new ChatProviderException(HttpStatus.BAD_GATEWAY);
        }
        return new ChatResponse(request.sessionId(), response);
    }

    private RuntimeException translateProviderFailure(RuntimeException exception) {
        // Spring AI and its retry layer may wrap the SDK exception.
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof ApiException apiException) {
                HttpStatus status = switch (apiException.code()) {
                    case 429 -> HttpStatus.TOO_MANY_REQUESTS;
                    case 503, 504 -> HttpStatus.SERVICE_UNAVAILABLE;
                    default -> HttpStatus.BAD_GATEWAY;
                };
                return new ChatProviderException(status);
            }
            if (cause instanceof IOException) {
                return new ChatProviderException(HttpStatus.SERVICE_UNAVAILABLE);
            }
        }
        return exception;
    }
}
