package br.com.commerceai.api.chat;

import br.com.commerceai.api.chat.memory.TemporaryChatMemory;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"spring.ai.model.chat=none", "GEMINI_API_KEY=test-only-not-a-real-key",
        "commerce.ai.memory.max-sessions=1"})
@AutoConfigureMockMvc
class ChatMemoryCapacityEndpointTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private TemporaryChatMemory memory;

    @MockitoBean
    private ChatModel model;

    @Test
    void returns503WhenAllMemorySlotsAreBusyWithoutCallingProvider() {
        memory.withSession("busy", session -> {
            try {
                mvc.perform(post("/api/v1/chat").contentType(MediaType.APPLICATION_JSON)
                                .content("{\"sessionId\":\"other\",\"message\":\"Oi\"}"))
                        .andExpect(status().isServiceUnavailable())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                        .andExpect(jsonPath("$.detail").value("Memoria temporaria ocupada. Tente novamente mais tarde."));
            } catch (Exception exception) {
                throw new IllegalStateException(exception);
            }
            return null;
        });
        verifyNoInteractions(model);
    }
}
