# Commerce AI — Roadmap de Desenvolvimento

**Versão:** 0.1.0\
**Status:** Fase 1 concluída; Fase 2 em planejamento\
**Metodologia:** Desenvolvimento incremental e aprendizado prático\
**Disponibilidade estimada:** 3 horas por dia, 6 dias por semana

## 1. Objetivo

Construir uma plataforma de assistente comercial inteligente para e-commerce, utilizando Java, Spring Boot e Engenharia de IA Generativa.

O desenvolvimento será organizado em entregas incrementais, com foco em funcionalidades reais, testes automatizados e evolução arquitetural.

Cada fase deverá resultar em uma funcionalidade demonstrável.

## 2. Princípios de execução

- Implementar uma funcionalidade por vez.
- Aprender os conceitos técnicos conforme forem necessários.
- Evitar dependências e infraestrutura prematuras.
- Priorizar serviços gratuitos.
- Criar testes para funcionalidades relevantes.
- Documentar decisões arquiteturais.
- Não avançar para a próxima fase sem validar a atual.
- Manter o projeto executável ao final de cada etapa.

## 3. Fases de desenvolvimento

### Fase 1 — Fundação e integração com LLM

**Status:** Concluída\
**Estimativa:** Semana 1

**Objetivo:** criar o backend inicial e realizar a primeira comunicação com o Gemini.

**Conceitos para estudar:**

- Arquitetura básica de LLMs.
- Tokens e janela de contexto.
- Prompt de sistema e prompt de usuário.
- Temperatura e parâmetros de geração.
- Spring AI ChatClient.
- Integração com APIs de modelos.

**Tarefas:**

- [x] Criar projeto Java 21 com Spring Boot.
- [x] Configurar Maven e dependências.
- [x] Configurar Spring AI.
- [x] Integrar Google Gemini.
- [x] Criar ChatController.
- [x] Criar ChatService.
- [x] Criar ChatRequest e ChatResponse.
- [x] Implementar POST /api/v1/chat.
- [x] Validar mensagens recebidas.
- [x] Implementar tratamento básico de erros.
- [x] Configurar chave de API por variável de ambiente.
- [x] Criar testes unitários.
- [x] Documentar execução local.

**Critérios de aceite:**

- Aplicação inicia sem erros.
- Endpoint recebe mensagens válidas.
- Integração com Gemini funciona.
- Resposta é retornada em JSON.
- Erros são tratados adequadamente.
- Credenciais não são expostas.
- Testes automatizados passam.

**Entrega:** API de chat funcional.

**Validação manual:** em 14/10/2026, o desenvolvedor confirmou resposta real do
Gemini 3.5 Flash-Lite e testes concluídos pelo Swagger e Postman.
Logs temporários de diagnóstico removidos após a validação.

---

### Fase 2 — Conversas e interface web

**Status:** Pendente\
**Estimativa:** Semana 2

**Conceitos para estudar:**

- Context window.
- Histórico de mensagens.
- Memória conversacional.
- Streaming de respostas.
- Server-Sent Events (SSE).

**Tarefas:**

- [ ] Criar identificação de sessões.
- [ ] Implementar histórico de mensagens.
- [ ] Adicionar memória conversacional.
- [ ] Implementar streaming.
- [ ] Criar frontend básico de chat.
- [ ] Implementar tratamento de erros na interface.
- [ ] Criar testes de continuidade da conversa.

**Critérios de aceite:**

- Cliente consegue conversar por uma interface web.
- Mensagens de uma sessão mantêm contexto.
- Sessões diferentes não compartilham histórico.
- Streaming funciona quando habilitado.

**Entrega:** Chat web com contexto conversacional.

---

### Fase 3 — Catálogo e Tool Calling

**Status:** Pendente\
**Estimativa:** Semana 3

**Conceitos para estudar:**

- Function calling.
- Tool calling.
- Structured outputs.
- Integração entre LLM e serviços Java.
- Validação de argumentos de ferramentas.

**Tarefas:**

- [ ] Criar módulo de produtos.
- [ ] Criar catálogo demonstrativo.
- [ ] Implementar busca por categoria.
- [ ] Implementar filtros por preço e tamanho.
- [ ] Criar ferramenta de consulta de produtos.
- [ ] Integrar ferramenta ao Spring AI.
- [ ] Impedir recomendações de produtos inexistentes.
- [ ] Criar testes de consultas.

**Critérios de aceite:**

- Assistente identifica requisitos do cliente.
- Consulta o catálogo por meio de ferramentas.
- Recomenda somente produtos encontrados.
- Não inventa preços ou disponibilidade.
- Trata consultas sem resultados.

**Entrega:** Assistente capaz de consultar produtos.

---

### Fase 4 — RAG e base de conhecimento

**Status:** Pendente\
**Estimativa:** Semana 4

**Conceitos para estudar:**

- Embeddings.
- Busca semântica.
- Vector databases.
- Chunking.
- Retrieval-Augmented Generation.
- Similaridade vetorial.
- Avaliação de recuperação.

**Tarefas:**

- [ ] Configurar PostgreSQL.
- [ ] Configurar pgvector.
- [ ] Criar pipeline de ingestão.
- [ ] Implementar processamento de documentos.
- [ ] Gerar embeddings.
- [ ] Persistir vetores.
- [ ] Implementar recuperação semântica.
- [ ] Integrar RAG ao chat.
- [ ] Retornar referências às fontes.
- [ ] Criar testes de recuperação.

**Critérios de aceite:**

- Documentos são indexados.
- Perguntas recuperam trechos relevantes.
- Respostas utilizam informações recuperadas.
- O assistente informa quando não encontra evidências suficientes.

**Entrega:** Chat com conhecimento documental.

---

### Fase 5 — MCP e integração de sistemas

**Status:** Pendente\
**Estimativa:** Semana 5

**Conceitos para estudar:**

- Model Context Protocol.
- MCP Server.
- MCP Client.
- Tools e Resources.
- Integração com APIs externas.
- Autorização de ferramentas.

**Tarefas:**

- [ ] Criar servidor MCP.
- [ ] Expor ferramentas de consulta.
- [ ] Implementar cliente MCP de teste.
- [ ] Criar ferramentas de estoque por unidade.
- [ ] Investigar API da Loja Integrada.
- [ ] Investigar integração com Softcom.
- [ ] Criar adaptadores para integrações.
- [ ] Implementar tratamento de falhas externas.

**Critérios de aceite:**

- Servidor MCP disponibiliza ferramentas.
- Cliente MCP consegue utilizá-las.
- Consultas retornam dados estruturados.
- Integrações indisponíveis possuem implementação simulada.
- Nenhuma ferramenta executa alterações sem autorização.

**Entrega:** Servidor MCP funcional com ferramentas comerciais.

---

### Fase 6 — Agentes e workflows

**Status:** Pendente\
**Estimativa:** Semana 6

**Conceitos para estudar:**

- AI Agents.
- Planejamento e execução.
- Tool orchestration.
- Workflows determinísticos.
- Human-in-the-loop.
- Controle de estado.
- Limites de execução.

**Tarefas:**

- [ ] Criar workflow de recomendação.
- [ ] Combinar consulta de produtos e estoque.
- [ ] Implementar tratamento de falhas.
- [ ] Adicionar limites de chamadas.
- [ ] Implementar confirmação para operações sensíveis.
- [ ] Criar testes de workflows.

**Critérios de aceite:**

- Assistente executa consultas em múltiplas etapas.
- Resultados são consistentes com as ferramentas.
- Falhas são tratadas.
- Operações sensíveis exigem confirmação.

**Entrega:** Assistente comercial com workflows inteligentes.

---

### Fase 7 — Segurança, observabilidade e deploy

**Status:** Pendente\
**Estimativa:** Semanas 7–8

**Conceitos para estudar:**

- LLMOps.
- Observabilidade.
- Métricas de latência e tokens.
- Prompt injection.
- Autenticação e autorização.
- Rate limiting.
- Docker.
- CI/CD.
- Deploy em nuvem.

**Tarefas:**

- [ ] Configurar segurança da API.
- [ ] Implementar rate limiting.
- [ ] Adicionar logs estruturados.
- [ ] Monitorar consumo de tokens.
- [ ] Implementar métricas e tracing.
- [ ] Criar testes de integração.
- [ ] Criar Dockerfile.
- [ ] Configurar Docker Compose.
- [ ] Criar pipeline de CI.
- [ ] Avaliar hospedagem gratuita disponível.
- [ ] Realizar deploy.
- [ ] Documentar operação.

**Critérios de aceite:**

- API protegida contra uso indevido.
- Testes automatizados passam.
- Métricas básicas disponíveis.
- Aplicação executável em contêiner.
- Deploy acessível em ambiente de demonstração.
- Nenhuma credencial é exposta.

**Entrega:** MVP demonstrável em ambiente de nuvem.

---

## 4. Backlog futuro

Funcionalidades fora do MVP inicial:

- [ ] Integração com WhatsApp.
- [ ] Integração com Instagram.
- [ ] Painel administrativo.
- [ ] Analytics de atendimento.
- [ ] Recomendações avançadas.
- [ ] Suporte a múltiplas empresas.
- [ ] Autenticação por empresa.
- [ ] Integração com marketplaces.
- [ ] Acompanhamento de pedidos.
- [ ] Métricas de conversão comercial.

Nenhuma dessas funcionalidades deve ser implementada sem priorização explícita.

## 5. Controle de progresso

Após concluir uma fase:

1. Marcar tarefas concluídas.
2. Registrar funcionalidades entregues.
3. Documentar problemas conhecidos.
4. Atualizar decisões arquiteturais.
5. Executar testes.
6. Definir a próxima tarefa.

O Codex não deverá marcar tarefas como concluídas sem evidência de implementação e validação.

## 6. Próxima atividade

**Fase atual:** Fase 2 — Conversas e interface web (ainda não implementada).

**Próximo passo:** apresentar o plano da memória conversacional temporária,
com limites, expiração e isolamento entre sessões, para aprovação antes de implementar.

Verificação: suíte automatizada com Java 21, 35 testes passando.
Documentação OpenAPI e Swagger UI implementados com contrato em interface `ChatApi`.
Integração real com Google Gemini confirmada pelo desenvolvedor via Swagger e Postman em 14/10/2026.

