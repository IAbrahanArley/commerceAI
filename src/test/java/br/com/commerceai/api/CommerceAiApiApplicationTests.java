package br.com.commerceai.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {"spring.ai.model.chat=none", "GEMINI_API_KEY=test-only-not-a-real-key"})
class CommerceAiApiApplicationTests {

    @MockitoBean
    private ChatModel chatModel;

	@Test
	void contextLoads() {
	}

}
