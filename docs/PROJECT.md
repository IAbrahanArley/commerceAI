# Commerce AI — Visão do Produto

**Nome:** Commerce AI  
**Categoria:** Inteligência Artificial aplicada ao varejo  
**Status:** Em desenvolvimento — MVP  
**Versão:** 0.1.0

---

## 1. Visão geral

O Commerce AI é uma plataforma de inteligência artificial voltada para lojas físicas e e-commerces.

Seu propósito é oferecer um assistente comercial inteligente capaz de compreender solicitações em linguagem natural, consultar informações comerciais e auxiliar clientes durante sua jornada de compra.

A solução será desenvolvida inicialmente como uma API backend utilizando Java, Spring Boot e Spring AI.

O projeto também possui finalidade educacional, permitindo aplicar conceitos modernos de Engenharia de IA em um cenário comercial realista.

## 2. Problema que queremos resolver

Pequenas e médias empresas do varejo enfrentam dificuldades como:

- Atendimento repetitivo sobre produtos, preços e disponibilidade.
- Clientes que abandonam compras por falta de orientação.
- Dificuldade para localizar produtos no estoque.
- Informações comerciais distribuídas entre diferentes sistemas.
- Pouca integração entre lojas físicas e e-commerce.
- Dificuldade para oferecer recomendações personalizadas em escala.

O Commerce AI pretende reduzir essas dificuldades por meio de um assistente conectado às informações da empresa.

## 3. Objetivos do produto

### Objetivo principal

Desenvolver um assistente comercial com IA que ajude consumidores a encontrar produtos, esclarecer dúvidas e avançar na jornada de compra.

### Objetivos específicos

1. Permitir conversas em linguagem natural.
2. Consultar produtos cadastrados no sistema.
3. Recomendar produtos de acordo com as necessidades dos clientes.
4. Consultar preços, tamanhos e disponibilidade.
5. Consultar estoque por unidade física.
6. Responder dúvidas sobre políticas comerciais.
7. Direcionar clientes para páginas de compra.
8. Integrar sistemas externos de gestão e e-commerce.
9. Disponibilizar uma arquitetura extensível para novos canais de atendimento.

## 4. Público-alvo

### Clientes comerciais

- Lojas de roupas e calçados.
- Lojas de artigos esportivos.
- Pequenos e médios e-commerces.
- Varejistas com múltiplas unidades físicas.

### Usuários finais

Consumidores que precisam de ajuda para localizar produtos, consultar disponibilidade ou tomar decisões de compra.

## 5. Case de referência — Loja de artigos esportivos

O projeto utilizará como referência um cenário comercial com:

- Duas lojas físicas.
- Plataforma de e-commerce Loja Integrada.
- Sistema de gestão Softcom.
- Estoque distribuído entre unidades.

### Exemplo de utilização

**Cliente:**

"Estou procurando um tênis para academia, tamanho 42, até R$ 300."

**Comportamento esperado:**

1. Interpretar a intenção do cliente.
2. Identificar categoria, tamanho e orçamento.
3. Consultar o catálogo de produtos.
4. Verificar preços e disponibilidade.
5. Apresentar opções compatíveis.
6. Informar a unidade com estoque, quando disponível.
7. Fornecer o link do produto no e-commerce.

**Regra fundamental:** o assistente nunca deverá inventar preços, produtos ou disponibilidade.

Se uma informação não estiver disponível, deverá comunicar essa limitação ao cliente.

## 6. Funcionalidades planejadas

### Módulo 1 — Chat inteligente

- Receber mensagens por API REST.
- Integrar um modelo de linguagem.
- Gerar respostas contextualizadas.
- Validar entradas.
- Tratar erros de comunicação.
- Gerenciar sessões e histórico em evolução futura.

### Módulo 2 — Catálogo de produtos

- Consultar produtos.
- Filtrar por categoria.
- Filtrar por preço.
- Filtrar por tamanho.
- Consultar características dos produtos.
- Disponibilizar ferramentas para o assistente consultar dados estruturados.

### Módulo 3 — Base de conhecimento (RAG)

- Indexar documentos comerciais.
- Recuperar informações relevantes.
- Responder dúvidas sobre políticas de troca, entrega e devolução.
- Utilizar informações recuperadas como contexto.
- Informar fontes quando disponíveis.

### Módulo 4 — Estoque

- Consultar disponibilidade por produto.
- Consultar estoque por unidade.
- Integrar fontes de dados externas.
- Diferenciar disponibilidade confirmada de informação indisponível.

### Módulo 5 — MCP

- Disponibilizar ferramentas comerciais através de um servidor MCP.
- Permitir que clientes MCP autorizados consultem funcionalidades do sistema.
- Integrar ferramentas externas quando necessário.

### Módulo 6 — Agentes de IA

- Executar workflows com múltiplas etapas.
- Combinar consultas a diferentes ferramentas.
- Interpretar intenções comerciais.
- Solicitar confirmação para operações sensíveis.
- Registrar ações realizadas.

### Módulo 7 — Interface web

- Disponibilizar uma interface de chat.
- Apresentar produtos recomendados.
- Exibir links para compra.
- Permitir continuidade das conversas.

## 7. Escopo do MVP

A primeira versão funcional terá como foco:

- API REST em Spring Boot.
- Integração com Google Gemini por meio do Spring AI.
- Endpoint de chat.
- Validação de mensagens.
- Tratamento de erros.
- Testes automatizados.
- Documentação de execução.

**O MVP inicial não incluirá:**

- Integrações reais com sistemas de estoque.
- Pagamentos.
- Processamento de pedidos.
- WhatsApp ou Instagram.
- Agentes autônomos.
- Arquitetura de microsserviços.

Essas funcionalidades serão avaliadas conforme a evolução do produto.

## 8. Requisitos funcionais

| ID | Requisito | Prioridade |
|---|---|---|
| RF-001 | Enviar mensagens ao assistente | Alta |
| RF-002 | Receber respostas geradas por LLM | Alta |
| RF-003 | Validar mensagens recebidas | Alta |
| RF-004 | Consultar produtos cadastrados | Alta |
| RF-005 | Filtrar produtos por características | Alta |
| RF-006 | Consultar disponibilidade de estoque | Alta |
| RF-007 | Responder com base em documentos | Média |
| RF-008 | Manter histórico de conversas | Média |
| RF-009 | Recomendar produtos | Média |
| RF-010 | Integrar plataformas externas | Futura |

## 9. Requisitos não funcionais

### Segurança

- Proteger credenciais e tokens.
- Validar dados de entrada.
- Não expor informações internas.
- Aplicar autenticação e autorização antes de disponibilizar funcionalidades protegidas.
- Prevenir operações não autorizadas por agentes.
- Considerar riscos de prompt injection.

### Desempenho

- Evitar chamadas desnecessárias aos modelos.
- Monitorar latência.
- Considerar streaming para melhorar a experiência.
- Controlar consumo de tokens.

### Manutenibilidade

- Utilizar Java 21 e Spring Boot.
- Seguir princípios SOLID.
- Manter módulos com responsabilidades claras.
- Implementar testes automatizados.
- Documentar decisões técnicas.

### Custos

- Priorizar ferramentas e serviços gratuitos na fase inicial.
- Monitorar consumo de APIs.
- Não contratar infraestrutura sem necessidade.
- Avaliar custos antes de colocar a aplicação em produção.

## 10. Tecnologias previstas

| Categoria | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework backend | Spring Boot |
| Integração com IA | Spring AI |
| LLM inicial | Google Gemini |
| Banco de dados futuro | PostgreSQL |
| Busca vetorial futura | pgvector |
| Protocolo de ferramentas | MCP |
| Containerização | Docker |
| Frontend futuro | React ou Next.js |
| Hospedagem | A definir |
| Versionamento | Git/GitHub |

As versões e dependências definitivas deverão ser registradas no documento ARCHITECTURE.md conforme forem adotadas.

## 11. Indicadores de sucesso

### Indicadores técnicos

- API de chat funcionando.
- Testes automatizados passando.
- Integração com LLM estável.
- Respostas fundamentadas em dados quando necessário.
- Logs e tratamento de erros adequados.
- Aplicação executável localmente e posteriormente na nuvem.

### Indicadores comerciais futuros

- Quantidade de atendimentos realizados.
- Percentual de consultas respondidas corretamente.
- Cliques em produtos recomendados.
- Conversões atribuíveis ao assistente, quando mensuráveis.
- Redução de atendimentos repetitivos.
- Satisfação dos usuários.

Esses indicadores serão medidos somente quando existirem dados reais suficientes.

## 12. Roadmap resumido

| Fase | Entrega |
|---|---|
| 1 | Backend e primeiro chatbot |
| 2 | Histórico de conversas e frontend |
| 3 | Catálogo e tool calling |
| 4 | RAG e base de conhecimento |
| 5 | MCP e integrações |
| 6 | Agentes e workflows |
| 7 | Segurança, observabilidade e deploy |

O planejamento detalhado deverá ser mantido em `docs/ROADMAP.md`.

## 13. Princípios do produto

1. **Informações confiáveis:** não inventar dados comerciais.
2. **IA como ferramenta:** utilizar LLMs quando agregarem valor real.
3. **Simplicidade:** evitar complexidade arquitetural prematura.
4. **Integração:** preservar a possibilidade de conectar sistemas existentes.
5. **Segurança:** proteger dados, credenciais e operações.
6. **Evolução incremental:** entregar funcionalidades pequenas e testáveis.
7. **Viabilidade comercial:** priorizar recursos que resolvam problemas reais do varejo.

## 14. Visão de longo prazo

Transformar o Commerce AI em uma solução reutilizável para diferentes empresas do varejo.

Possíveis evoluções:

- Atendimento multicanal.
- Integração com marketplaces.
- Recomendações contextualizadas.
- Automação de processos comerciais.
- Painel administrativo.
- Analytics de atendimento e conversão.
- Suporte a múltiplas empresas, com isolamento de dados.

Essas possibilidades não fazem parte do escopo inicial e dependerão de validação comercial e técnica.

---

**Estado atual:** Fase 1 concluída, com endpoint de chat, validação, tratamento de erros,
testes automatizados e chamada real ao Gemini confirmada pelo desenvolvedor via Swagger e Postman.

**Próximo marco:** planejar e implementar memória conversacional temporária, após aprovação do plano.

