package br.com.commerceai.api.shared.exception;

import br.com.commerceai.api.chat.service.ChatProviderException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, "Requisicao invalida.");
        problem.setProperty("errors", exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldError(error.getField(), error.getDefaultMessage())).toList());
        return handleExceptionInternal(exception, problem, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException exception, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        return handleExceptionInternal(exception,
                ProblemDetail.forStatusAndDetail(status, "Corpo JSON ausente ou invalido."),
                headers, status, request);
    }

    @ExceptionHandler(ChatProviderException.class)
    public ProblemDetail handleProvider(ChatProviderException exception) {
        String detail = switch (exception.status()) {
            case TOO_MANY_REQUESTS -> "Limite de chamadas ao provedor atingido. Tente novamente mais tarde.";
            case SERVICE_UNAVAILABLE -> "Assistente temporariamente indisponivel. Tente novamente mais tarde.";
            default -> "Nao foi possivel obter uma resposta valida do provedor.";
        };
        return ProblemDetail.forStatusAndDetail(exception.status(), detail);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro interno ao processar a requisicao.");
    }

    private record FieldError(String field, String message) {
    }
}
