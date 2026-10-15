package br.com.commerceai.api.chat.memory;

public class ChatMemoryCapacityException extends RuntimeException {

    public ChatMemoryCapacityException() {
        super("Todas as sessoes de memoria temporaria estao em uso.");
    }
}
