package br.com.commerceai.api.chat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import com.google.genai.errors.ApiException;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"spring.ai.model.chat=none", "GEMINI_API_KEY=test-only-not-a-real-key"})
@AutoConfigureMockMvc
class ChatEndpointTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ChatModel chatModel;

    @BeforeEach
    void setUp() {
        when(chatModel.getOptions()).thenReturn(ChatOptions.builder().build());
    }

    @Test
    void returnsModelResponseAndCorrelationId() throws Exception {
        when(chatModel.call(any(Prompt.class))).thenReturn(new ChatResponse(
                List.of(new Generation(new AssistantMessage("Qual e o seu orcamento?")))));
        mvc.perform(post("/api/v1/chat").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sessionId\":\"session-001\",\"message\":\"Quero um tenis\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value("session-001"))
                .andExpect(jsonPath("$.response").value("Qual e o seu orcamento?"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"sessionId\":\"s\"}", "{\"message\":\"Oi\"}",
            "{\"sessionId\":\"s\",\"message\":\"   \"}",
            "{\"sessionId\":\"invalid session\",\"message\":\"Oi\"}",
            "{\"sessionId\":\"\",\"message\":\"Oi\"}"})
    void rejectsInvalidFieldsWithoutCallingModel(String json) throws Exception {
        mvc.perform(post("/api/v1/chat").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errors").isArray());
        verifyNoInteractions(chatModel);
    }

    @Test
    void rejectsOversizedFields() throws Exception {
        for (Map<String, String> body : List.of(
                Map.of("sessionId", "s", "message", "a".repeat(4001)),
                Map.of("sessionId", "s".repeat(65), "message", "Oi"))) {
            mvc.perform(post("/api/v1/chat").contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(chatModel);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "{broken"})
    void rejectsMissingOrMalformedJson(String json) throws Exception {
        mvc.perform(post("/api/v1/chat").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Corpo JSON ausente ou invalido."));
        verifyNoInteractions(chatModel);
    }

    @Test
    void doesNotExposeUnexpectedExceptionDetails() throws Exception {
        when(chatModel.call(any(Prompt.class))).thenThrow(new IllegalStateException("sensitive-detail"));
        mvc.perform(post("/api/v1/chat").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sessionId\":\"s\",\"message\":\"Oi\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.detail").value("Ocorreu um erro interno ao processar a requisicao."))
                .andExpect(jsonPath("$.trace").doesNotExist());
    }

    @Test
    void preservesHttpMethodErrors() throws Exception {
        mvc.perform(get("/api/v1/chat")).andExpect(status().isMethodNotAllowed());
    }

    @ParameterizedTest
    @CsvSource({"429, 429", "503, 503", "504, 503", "401, 502", "500, 502"})
    void returnsSafeProviderError(int providerCode, int expectedStatus) throws Exception {
        when(chatModel.call(any(Prompt.class))).thenThrow(new RuntimeException(
                new ApiException(providerCode, "provider-status", "sensitive-detail")));
        mvc.perform(post("/api/v1/chat").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sessionId\":\"s\",\"message\":\"Oi\"}"))
                .andExpect(status().is(expectedStatus))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("sensitive-detail"))));
    }

    @Test
    void generatesOpenApiFromInterfaceContract() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Commerce AI API"))
                .andExpect(jsonPath("$.paths['/api/v1/chat'].post.operationId").value("sendChatMessage"))
                .andExpect(jsonPath("$.paths['/api/v1/chat'].post.tags[0]").value("Chat"))
                .andExpect(jsonPath("$.paths['/api/v1/chat'].post.requestBody.required").value(true))
                .andExpect(jsonPath("$.paths['/api/v1/chat'].post.responses['200'].content['application/json'].schema['$ref']")
                        .value("#/components/schemas/ChatResponse"))
                .andExpect(jsonPath("$.paths['/api/v1/chat'].post.responses['400'].content['application/problem+json'].schema['$ref']")
                        .value("#/components/schemas/ProblemDetail"))
                .andExpect(jsonPath("$.paths['/api/v1/chat'].post.responses['429']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/chat'].post.responses['500']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/chat'].post.responses['502']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/chat'].post.responses['503']").exists())
                .andExpect(jsonPath("$.components.schemas.ChatRequest.properties.message.maxLength").value(4000))
                .andExpect(jsonPath("$.components.schemas.ChatRequest.properties.sessionId.maxLength").value(64));
    }

    @Test
    void servesSwaggerUiAndItsConfiguration() throws Exception {
        mvc.perform(get("/swagger-ui.html"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/swagger-ui/index.html"));
        mvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));
        mvc.perform(get("/v3/api-docs/swagger-config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("/v3/api-docs"));
    }
}
