package br.com.commerceai.api.chat.controller;

import br.com.commerceai.api.chat.dto.ChatRequest;
import br.com.commerceai.api.chat.dto.ChatResponse;
import br.com.commerceai.api.chat.service.ChatService;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController implements ChatApi {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @Override
    public ChatResponse chat(ChatRequest request) {
        return chatService.chat(request);
    }
}
