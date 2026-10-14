# Instrucoes permanentes para agentes

## Contexto do projeto

Commerce AI e uma plataforma de assistente comercial inteligente para e-commerce, desenvolvida com Java 21, Spring Boot e Spring AI.

## Perfil do usuario

- O usuario e desenvolvedor Java Pleno e esta aprendendo Engenharia de IA na pratica.
- Responda sempre em portugues brasileiro.
- Explique conceitos novos como LLM, RAG, MCP, embeddings e tool calling quando forem utilizados.

## Diretrizes de desenvolvimento

- Priorize boas praticas de Java, SOLID, Clean Code e testes automatizados.
- Utilize arquitetura modular, evitando abstracoes e microsservicos desnecessarios.
- Nao implemente funcionalidades futuras sem solicitacao.
- Nao adicione dependencias sem explicar a necessidade.
- Priorize solucoes gratuitas durante o desenvolvimento.
- Nunca exponha credenciais ou dados sensiveis.
- Antes de mudancas arquiteturais significativas, apresente um plano e solicite aprovacao.
- Apos implementar funcionalidades, execute os testes disponiveis e informe os resultados.
- Mantenha os arquivos de documentacao atualizados.

## Padrao de controllers e documentacao da API

- Para endpoints REST, crie uma interface com sufixo `Api` no pacote de controllers da funcionalidade.
- Centralize nessa interface as assinaturas, os mapeamentos HTTP, `@Valid`, `@RequestBody` e a documentacao OpenAPI (`@Tag`, `@Operation`, `@ApiResponses`).
- A classe com sufixo `Controller` deve implementar a interface, usar `@RestController`, receber servicos por construtor e delegar o processamento, sem duplicar as anotacoes do contrato.
- Mantenha as restricoes de validacao nos DTOs e documente exemplos, limites e respostas de erro reais.
- Utilize springdoc-openapi para gerar a especificacao e o Swagger UI. Teste o contrato HTTP e a especificacao gerada apos refatoracoes.
- Referencia escolhida pelo usuario: https://github.com/IAbrahanArley/project-rh/blob/main/backend/src/main/java/br/com/abrahanarley/controllers/CandidateResumeApi.java

