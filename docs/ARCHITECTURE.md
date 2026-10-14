# Commerce AI — Arquitetura Técnica

**Versão:** 0.1.0  
**Status:** Endpoint inicial implementado; evoluções propostas  
**Estilo:** Monólito modular  
**Linguagem:** Java 21

## 1. Visão arquitetural

O Commerce AI será desenvolvido inicialmente como uma aplicação backend monolítica modular, utilizando Java, Spring Boot e Spring AI.

A aplicação será responsável por receber mensagens dos usuários, orquestrar chamadas a modelos de linguagem e, futuramente, consultar serviços comerciais, documentos e ferramentas externas.

### Princípios

- Arquitetura simples e evolutiva.
- Separação de responsabilidades.
- Baixo acoplamento.
- Alta coesão.
- Desenvolvimento orientado a funcionalidades.
- Testabilidade.
- Segurança desde o início.
- Evitar overengineering.

Não utilizaremos microsserviços, mensageria ou infraestrutura distribuída sem necessidade concreta.

## 2. Stack tecnológica

### Tecnologias iniciais

| Tecnologia | Responsabilidade |
|---|---|
| Java 21 | Linguagem principal |
| Spring Boot 4.1.1 | Framework backend |
| Spring Web | API REST |
| Spring Validation | Validação |
| Spring AI 2.0.1 | Integração com LLM |
| Google Gemini | Provedor inicial de IA |
| Spring Boot Actuator | Health checks e métricas |
| Maven 3.10.0 (Wrapper) | Build e dependências |
| JUnit Jupiter 6.0.3 | Testes automatizados |

As versões definitivas de Spring Boot e Spring AI deverão ser compatíveis entre si e verificadas no pom.xml.

### Tecnologias futuras

| Tecnologia | Utilização prevista |
|---|---|
| PostgreSQL | Persistência |
| pgvector | Busca vetorial |
| Flyway | Migrações |
| Spring Security | Segurança |
| MCP Java SDK / Spring AI MCP | Ferramentas MCP |
| Docker | Containerização |
| OpenTelemetry | Observabilidade |
| React / Next.js | Frontend |

Essas tecnologias não devem ser adicionadas antecipadamente.

## 3. Organização de pacotes

Adotaremos organização por funcionalidade, evitando concentrar todos os controllers, services e repositories em pacotes globais.

Estrutura inicial:

```text
src/main/java/br/com/commerceai/api/
├── CommerceAiApiApplication.java
├── chat/
│   ├── controller/
│   │   └── ChatController.java
│   ├── service/
│   │   └── ChatService.java
│   └── dto/
│       ├── ChatRequest.java
│       └── ChatResponse.java
├── config/
│   └── AiConfig.java
└── shared/
    └── exception/
        └── GlobalExceptionHandler.java
```

A classe AiConfig configura o cliente Google GenAI com timeout de 30 segundos e uma tentativa.
O Spring AI também utiliza uma tentativa. Não há repetição automática nesta etapa.

### Módulos futuros

```text
br.com.commerceai.api
├── chat/
├── catalog/
├── inventory/
├── knowledge/
├── integration/
├── agent/
├── config/
└── shared/
```

Cada módulo deverá possuir suas próprias classes de aplicação, domínio e infraestrutura conforme sua complexidade justificar.

## 4. Fluxo inicial de requisição

```text
Cliente HTTP
     |
     v
ChatController
     |
     v
ChatService
     |
     v
Spring AI ChatClient
     |
     v
Google Gemini API
     |
     v
Resposta do modelo
     |
     v
ChatResponse
```

### Responsabilidades

**ChatController**
- Receber requisições HTTP.
- Validar dados de entrada.
- Delegar processamento ao serviço.
- Retornar respostas HTTP.

**ChatService**
- Coordenar a geração de respostas.
- Aplicar instruções de comportamento.
- Utilizar o ChatClient.
- Tratar o resultado da interação.

**ChatClient**
- Abstrair a comunicação com o modelo.
- Enviar mensagens e configurações.
- Receber respostas do provedor.

**GlobalExceptionHandler**
- Padronizar respostas de erro.
- Evitar exposição de detalhes internos.
- Tratar falhas conhecidas da API.

## 5. Contrato inicial da API

### POST /api/v1/chat

Request:

```json
{
  "sessionId": "session-001",
  "message": "Quero um tênis para academia"
}
```

Response:

```json
{
  "sessionId": "session-001",
  "response": "Olá! Qual é o seu orçamento?"
}
```

O campo sessionId será inicialmente um identificador de correlação fornecido pelo cliente.

Não deverá ser interpretado como memória conversacional persistente na primeira versão.

### Validações

- message obrigatório.
- message não pode ser vazio.
- Definir limite máximo de caracteres.
- sessionId deverá seguir formato e tamanho definidos.
- Entradas inválidas devem retornar HTTP 400.

Implementado: `message` tem limite de 4000 caracteres e não aceita conteúdo em branco.
`sessionId` é obrigatório, tem limite de 64 caracteres e aceita `[a-zA-Z0-9_-]+`.
Erros usam `ProblemDetail` (`application/problem+json`), com erros de campo nas falhas
de validação e sem expor valores rejeitados ou mensagens internas.

### Tratamento de erros

- 400: requisição inválida.
- 429: limite de requisições, quando aplicável.
- 502: falha na comunicação com provedor externo.
- 503: indisponibilidade temporária.
- 500: erro interno inesperado.

Os códigos deverão refletir a causa efetiva da falha, sem expor credenciais ou detalhes internos.

## 6. Integração com LLM

O provedor inicial será Google Gemini, utilizando Spring AI.

### Regras

- Credenciais em variáveis de ambiente.
- Não registrar prompts sensíveis em logs.
- Configurar limites de saída.
- Tratar indisponibilidade e timeouts.
- Monitorar consumo quando possível.
- Não presumir que respostas do LLM são factualmente corretas.
- Não permitir que o modelo execute ações sem validação.

### Evolução

Caso seja necessário utilizar múltiplos provedores, introduzir uma abstração específica para a aplicação.

Não criar uma camada genérica de múltiplos provedores antes de existir necessidade.

## 7. Estratégia de Tool Calling

Informações comerciais estruturadas deverão ser consultadas por serviços e ferramentas.

Exemplos:

- searchProducts
- getProductDetails
- checkInventory
- getStoreInformation

As ferramentas deverão:

- Validar argumentos.
- Respeitar permissões.
- Retornar dados estruturados.
- Tratar falhas externas.
- Não confiar em parâmetros produzidos pelo LLM sem validação.

Preços e estoque não deverão ser inventados pelo modelo.

## 8. Estratégia de RAG

RAG será utilizado para consultar conhecimento documental.

Exemplos:

- Políticas de troca.
- Políticas de devolução.
- Informações sobre entrega.
- Documentação comercial.
- Guias de produtos.

### Pipeline previsto

```text
Documento
   |
   v
Extração de texto
   |
   v
Chunking
   |
   v
Embeddings
   |
   v
PostgreSQL + pgvector
   |
   v
Busca semântica
   |
   v
Contexto para o LLM
```

### Diretrizes

- Preservar metadados dos documentos.
- Permitir identificação das fontes.
- Avaliar qualidade da recuperação.
- Não utilizar RAG como fonte primária para estoque em tempo real.
- Não enviar documentos confidenciais sem autorização.

## 9. Estratégia de MCP

O MCP será introduzido quando existirem ferramentas comerciais funcionais.

O servidor MCP deverá expor funcionalidades de maneira controlada.

Exemplos:

- Consulta de produtos.
- Consulta de disponibilidade.
- Consulta de informações de lojas.

A implementação deverá reutilizar serviços de aplicação existentes.

Não duplicar regras de negócio dentro das ferramentas MCP.

MCP não será obrigatório para chamadas internas do Spring AI; sua adoção deverá demonstrar benefício de interoperabilidade.

## 10. Persistência

A persistência será introduzida quando necessária.

### Entidades previstas

**Product**
- id
- sku
- name
- description
- category
- price
- active

**Store**
- id
- name
- location

**Inventory**
- id
- productId
- storeId
- quantity

**Conversation**
- id
- createdAt
- updatedAt

**Message**
- id
- conversationId
- role
- content
- createdAt

Os modelos acima são conceituais e não representam entidades implementadas.

O esquema definitivo deverá considerar integridade, índices, privacidade e necessidades reais.

## 11. Segurança

### Regras gerais

- Nunca versionar chaves de API.
- Utilizar variáveis de ambiente.
- Validar entradas.
- Não expor stack traces ao cliente.
- Não registrar dados sensíveis desnecessariamente.
- Implementar autenticação antes de expor dados privados.
- Adicionar rate limiting antes de disponibilizar o chat publicamente.
- Aplicar autorização nas ferramentas.
- Tratar conteúdo recuperado por RAG como não confiável.
- Considerar ataques de prompt injection.

### Dados comerciais

Operações de consulta poderão ser automatizadas dentro das permissões definidas.

Operações que alterem pedidos, preços, estoque ou dados de clientes exigirão autorização explícita e validação no backend.

## 12. Testes

### Testes unitários

- Serviços.
- Validações.
- Regras comerciais.
- Tratamento de erros.

### Testes de integração

- Endpoints REST.
- Persistência.
- Integração com ferramentas.
- Contratos de serviços externos.

Chamadas reais ao Gemini não deverão ser necessárias para executar a suíte de testes padrão.

Utilizar mocks ou implementações de teste para garantir execução determinística.

### Avaliações de IA

Futuramente, criar cenários de avaliação para:

- Respostas fundamentadas.
- Produtos inexistentes.
- Estoque indisponível.
- Perguntas fora do contexto.
- Tentativas de manipulação de instruções.
- Utilização correta de ferramentas.

## 13. Observabilidade

Evolução planejada:

- Logs estruturados.
- Health checks.
- Métricas de latência.
- Quantidade de chamadas ao LLM.
- Consumo de tokens.
- Taxa de erros.
- Tracing de requisições.
- Métricas de ferramentas e recuperação RAG.

Logs não deverão armazenar dados sensíveis sem necessidade e autorização.

## 14. Infraestrutura e deploy

### Desenvolvimento local

- Aplicação Spring Boot.
- Variáveis de ambiente.
- Banco PostgreSQL em Docker quando necessário.
- Serviços simulados para integrações indisponíveis.

### Ambiente de demonstração

- Container Docker.
- Hospedagem a definir.
- Configuração segura de segredos.
- Health checks.
- Logs.
- Controle de acesso e consumo.

A plataforma de hospedagem será escolhida considerando custo, limites gratuitos e requisitos da aplicação.

Nenhuma infraestrutura paga deverá ser provisionada sem aprovação.

## 15. Decisões arquiteturais

As decisões importantes deverão ser registradas por meio de Architecture Decision Records (ADRs).

Formato sugerido:

```text
docs/adr/
├── 0001-modular-monolith.md
├── 0002-spring-ai.md
└── 0003-llm-provider.md
```

Cada ADR deverá registrar:

- Contexto.
- Decisão.
- Alternativas consideradas.
- Justificativa.
- Consequências.
- Status.

### Decisões iniciais propostas

**ADR-001 — Monólito modular**

Motivo: reduzir complexidade operacional e permitir evolução incremental.

**ADR-002 — Spring AI**

Motivo: integração natural com o ecossistema Java e Spring Boot.

**ADR-003 — Google Gemini**

Motivo: permitir experimentação inicial com modalidade gratuita, respeitando limites e condições do provedor.

Essas decisões deverão ser formalizadas conforme a implementação avançar.

## 16. Evolução arquitetural

A arquitetura deverá evoluir conforme necessidades comprovadas.

Não introduzir:

- Microsserviços sem necessidade de isolamento.
- Kafka ou RabbitMQ sem demanda assíncrona.
- Kubernetes sem justificativa operacional.
- Cache distribuído sem gargalo identificado.
- Multiagentes quando um workflow simples resolver.
- Múltiplos bancos sem necessidade.
- Abstrações genéricas sem casos de uso concretos.

## 17. Estado atual

**Implementado:** endpoint `POST /api/v1/chat`, DTOs imutáveis, validação, serviço
com `ChatClient`, prompt de sistema, credenciais por ambiente, timeout,
tratamento de erros e testes automatizados sem chamadas externas.

**Planejado:** memória conversacional, catálogo, ferramentas, RAG e demais módulos.

**Validação:** build limpo com Java 21 e 28 testes passando, sem falhas ou erros.

**Validação externa pendente:** confirmar autenticação e resposta real do Gemini
com uma chave válida. A inicialização do modelo é testada sem enviar mensagens.

Execução e conceitos iniciais estão documentados no `README.md`.

Este documento deverá ser atualizado conforme a arquitetura real do repositório evoluir.

