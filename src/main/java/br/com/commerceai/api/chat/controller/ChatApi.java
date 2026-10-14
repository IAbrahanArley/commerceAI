package br.com.commerceai.api.chat.controller;

import br.com.commerceai.api.chat.dto.ChatRequest;
import br.com.commerceai.api.chat.dto.ChatResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Chat", description = "Interacoes com o assistente comercial Commerce AI.")
@RequestMapping("/api/v1/chat")
public interface ChatApi {

    @Operation(
            operationId = "sendChatMessage",
            summary = "Enviar uma mensagem ao assistente",
            description = """
                    Gera uma resposta com o Gemini. sessionId e obrigatorio, aceita letras ASCII,
                    numeros, _ e -, e tem limite de 64 caracteres. message e obrigatoria,
                    nao pode conter apenas espacos e tem limite de 4000 caracteres.
                    sessionId serve apenas para correlacao; nao ha memoria conversacional.
                    Nesta versao o assistente nao consulta catalogo, precos ou estoque.
                    A chamada ao modelo externo pode consumir a cota configurada no backend.
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ChatRequest.class),
                            examples = @ExampleObject(value = """
                                    {"sessionId":"session-001","message":"Quero um tenis para academia"}
                                    """))))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resposta gerada pelo assistente.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ChatResponse.class),
                            examples = @ExampleObject(value = """
                                    {"sessionId":"session-001","response":"Qual e o seu orcamento?"}
                                    """))),
            @ApiResponse(responseCode = "400", description = "JSON invalido ou campos fora das regras de validacao. "
                    + "Erros de campo sao informados na propriedade errors.",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = """
                                    {"type":"about:blank","title":"Bad Request","status":400,
                                    "detail":"Requisicao invalida.","instance":"/api/v1/chat",
                                    "errors":[{"field":"message","message":"message e obrigatoria"}]}
                                    """))),
            @ApiResponse(responseCode = "429", description = "Limite de chamadas ao provedor atingido.",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno inesperado.",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "502", description = "Falha do provedor ou resposta sem conteudo valido.",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "503", description = "Provedor indisponivel, timeout ou falha de rede.",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    ChatResponse chat(@Valid @RequestBody ChatRequest request);
}
