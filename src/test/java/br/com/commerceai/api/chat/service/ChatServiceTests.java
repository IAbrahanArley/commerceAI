package br.com.commerceai.api.chat.service;

import br.com.commerceai.api.chat.dto.ChatRequest;
import com.google.genai.errors.ApiException;
import java.io.InterruptedIOException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.http.HttpStatus;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ChatServiceTests {

    private ChatModel model;
    private ChatService service;

    @BeforeEach
    void setUp() {
        model = mock(ChatModel.class);
        when(model.getOptions()).thenReturn(ChatOptions.builder().build());
        service = new ChatService(ChatClient.builder(model));
    }

    @Test
    void separatesSystemAndUserMessagesWithoutSharingConversationHistory() {
        when(model.call(any(Prompt.class))).thenReturn(new ChatResponse(
                List.of(new Generation(new AssistantMessage("Como posso ajudar?")))));
        service.chat(new ChatRequest("s", "Primeira mensagem"));
        var response = service.chat(new ChatRequest("s", "Segunda mensagem"));
        assertThat(response.sessionId()).isEqualTo("s");
        assertThat(response.response()).isEqualTo("Como posso ajudar?");
        var captor = ArgumentCaptor.forClass(Prompt.class);
        verify(model, times(2)).call(captor.capture());
        Prompt prompt = captor.getValue();
        assertThat(prompt.getInstructions()).hasSize(2);
        assertThat(prompt.getSystemMessage().getText()).contains("Nunca invente produtos");
        assertThat(prompt.getUserMessage().getText()).isEqualTo("Segunda mensagem");
    }

    @ParameterizedTest
    @CsvSource({"429, TOO_MANY_REQUESTS", "503, SERVICE_UNAVAILABLE", "504, SERVICE_UNAVAILABLE",
            "401, BAD_GATEWAY", "500, BAD_GATEWAY"})
    void translatesWrappedProviderErrors(int code, HttpStatus status) {
        when(model.call(any(Prompt.class))).thenThrow(new RuntimeException("wrapper",
                new ApiException(code, "provider-status", "sensitive-detail")));
        assertThatThrownBy(() -> service.chat(new ChatRequest("s", "Oi")))
                .isInstanceOfSatisfying(ChatProviderException.class,
                        exception -> assertThat(exception.status()).isEqualTo(status));
    }

    @Test
    void translatesNetworkTimeout() {
        when(model.call(any(Prompt.class))).thenThrow(new RuntimeException(new InterruptedIOException("timeout")));
        assertThatThrownBy(() -> service.chat(new ChatRequest("s", "Oi")))
                .isInstanceOfSatisfying(ChatProviderException.class,
                        exception -> assertThat(exception.status()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE));
    }

    @Test
    void rejectsMissingModelContent() {
        when(model.call(any(Prompt.class))).thenReturn(new ChatResponse(List.of()));
        assertThatThrownBy(() -> service.chat(new ChatRequest("s", "Oi")))
                .isInstanceOf(ChatProviderException.class);
    }

    @Test
    void rejectsBlankModelContent() {
        when(model.call(any(Prompt.class))).thenReturn(new ChatResponse(
                List.of(new Generation(new AssistantMessage("   ")))));
        assertThatThrownBy(() -> service.chat(new ChatRequest("s", "Oi")))
                .isInstanceOf(ChatProviderException.class);
    }
}
