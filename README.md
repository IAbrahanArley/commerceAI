# Commerce AI

API de assistente comercial em Java 21, Spring Boot 4.1.1 e Spring AI 2.0.1.

## Executar localmente

Requisitos: JDK 21, acesso ao Maven Central e uma chave da Gemini Developer API.
O Maven Wrapper baixa o Maven 3.10.0 na primeira execucao.

No PowerShell, configure o JDK instalado e a chave no ambiente da sessao:

```powershell
$env:JAVA_HOME = 'C:/Users/abrah/.jdks/corretto-21.0.9'
$env:PATH = "$env:JAVA_HOME/bin;$env:PATH"
$env:GEMINI_API_KEY = '<sua-chave-local>'
./mvnw.cmd spring-boot:run
```

Nao registre a chave em arquivos versionados, capturas ou historico compartilhado.
O modelo padrao e `gemini-3.5-flash-lite`; use `GEMINI_MODEL` para substitui-lo.
Se essa variavel ainda apontar para `gemini-2.5-flash` ou `gemini-3.8-flash`, atualize-a ou remova-a,
pois ela tem prioridade sobre o modelo padrao do YAML.
Escolhemos Flash-Lite para os testes iniciais por seu foco em baixa latencia e pela
modalidade gratuita publicada pelo provedor. A disponibilidade e as cotas dependem do projeto.
Um erro 503 com mensagem de alta demanda indica indisponibilidade temporaria do modelo;
aguarde antes de tentar novamente ou altere `GEMINI_MODEL`. Nao ha fallback automatico.
Verifique cotas e condicoes da modalidade gratuita no provedor antes de usar.
A aplicacao exige uma chave configurada para iniciar normalmente.
`AiConfig` nao registra a chave ou trechos dela em logs; o valor enviado ao SDK nao e alterado.
O fluxo de configuracao e `GEMINI_API_KEY` -> propriedade YAML
`spring.ai.google.genai.api-key` -> parametro `apiKey` de `AiConfig`.
A variavel deve conter a chave real, e nao o texto `${GEMINI_API_KEY}` ou
`${spring.ai.google.genai.api-key}`. Valores vazios ou placeholders nao resolvidos
sao rejeitados antes de criar o cliente. Reinicie a aplicacao apos alterar as variaveis.

## Chat

`POST http://localhost:8080/api/v1/chat`

Collection do Postman: [docs/postman/commerce-ai-api.postman_collection.json](docs/postman/commerce-ai-api.postman_collection.json)

```json
{
  "sessionId": "session-001",
  "message": "Quero um tenis para academia"
}
```

Resposta de exemplo (o texto e gerado pelo modelo e pode variar):

```json
{
  "sessionId": "session-001",
  "response": "Qual e o seu orcamento?"
}
```

Exemplo no PowerShell:

```powershell
$body = @{ sessionId = 'session-001'; message = 'Quero um tenis para academia' } | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/api/v1/chat' -ContentType 'application/json' -Body $body
```

`sessionId` e obrigatorio, aceita letras ASCII, numeros, `_` e `-`, e tem limite de 64 caracteres.
`message` e obrigatoria, nao pode conter apenas espacos e tem limite de 4000 caracteres.
O identificador seleciona a memoria temporaria da conversa e e devolvido na resposta.
Use o mesmo `sessionId` para continuar a conversa e um ID diferente para iniciar outra.

## Memoria temporaria

O LLM nao guarda as interacoes sozinho: a API envia a janela de mensagens anteriores
como contexto junto com a nova pergunta. Isso aumenta o consumo de tokens de entrada.
Usamos `MessageWindowChatMemory` do Spring AI, sem novas dependencias.

- Ate 20 mensagens por sessao (aproximadamente 10 pares pergunta/resposta).
- Expiracao apos 30 minutos de inatividade, contados da ultima requisicao concluida.
- Ate 1000 sessoes por instancia; ao atingir esse limite, a sessao ociosa mais antiga e removida.
- Sessoes em uso ou com chamadas aguardando processamento nao sao removidas.
- Se todas as sessoes estiverem em uso, uma nova sessao recebe HTTP 503 sem chamar o Gemini.
- Chamadas simultaneas da mesma sessao sao serializadas; sessoes diferentes podem executar em paralelo.
- Apenas pares pergunta/resposta validos sao armazenados; falhas e respostas vazias nao alteram o historico.

O prompt de sistema e enviado em toda chamada e nao faz parte das 20 mensagens guardadas.
Reiniciar a aplicacao apaga a memoria. Ela tambem nao e compartilhada entre instancias.
O historico completo nao e persistido. Mensagens expiradas sao removidas no proximo acesso
ou pela limpeza automatica executada a cada minuto, quando a sessao estiver ociosa.

Configuracao opcional por variaveis de ambiente:

| Variavel | Padrao | Regra |
|---|---|---|
| `CHAT_MEMORY_MAX_MESSAGES` | `20` | Pelo menos 2 |
| `CHAT_MEMORY_MAX_SESSIONS` | `1000` | Positivo |
| `CHAT_MEMORY_TTL` | `30m` | Duracao positiva, por exemplo `5m` |

Teste manual: envie `Meu orcamento e 300 reais e meu tamanho e 42` e depois
`Qual foi o orcamento e o tamanho que informei?`, mantendo o mesmo `sessionId`.
Repita a segunda pergunta com outro ID: o contexto da primeira conversa nao sera enviado.
A collection Postman inclui um grupo com esses tres passos e IDs gerados para cada rodada.

O `sessionId` nao autentica o usuario. Quem usar o mesmo ID acessara o mesmo contexto.
Use identificadores aleatorios distintos, sem dados pessoais, e mantenha o uso local
ate implementarmos autenticacao e vinculacao da sessao ao usuario.

Erros usam `application/problem+json`: 400 para JSON ou campos invalidos, 429 para cota
do provedor, 502 para falha do provedor ou resposta vazia, 503 para indisponibilidade
ou falha de rede e 500 para erro inesperado. Falta de capacidade da memoria tambem retorna 503.
Detalhes internos nao sao devolvidos.
As chamadas possuem timeout de 60 segundos e uma tentativa por camada, sem repeticoes automaticas.
Use `GEMINI_TIMEOUT_MS` para ajustar esse limite em milissegundos; o valor deve ser positivo.
Se ocorrer `InterruptedIOException: timeout`,
o cliente cancelou a espera; isso pode refletir latencia do modelo ou problemas de rede.

O endpoint ainda nao possui autenticacao ou limitacao local de chamadas; utilize-o em desenvolvimento local.
Nao ha consultas a produtos, estoque ou politicas comerciais nesta etapa.

## Swagger e OpenAPI

Com a aplicacao em execucao:

- Swagger UI: http://localhost:8080/swagger-ui.html
- Especificacao OpenAPI JSON: http://localhost:8080/v3/api-docs
- Especificacao OpenAPI YAML: http://localhost:8080/v3/api-docs.yaml

OpenAPI e o contrato legivel por ferramentas, com operacoes, schemas e respostas.
Swagger UI apresenta esse contrato e permite enviar requisicoes pelo navegador.
O springdoc-openapi 3.1.1 gera ambos a partir das anotacoes da aplicacao.

O contrato do chat esta em `ChatApi`: assinatura, mapeamentos HTTP, validacao de entrada
e anotacoes de documentacao. `ChatController` implementa a interface e delega ao servico.
As restricoes dos campos continuam nos DTOs; o endpoint e o comportamento permanecem iguais.
Esse padrao esta registrado em `AGENTS.md` para os proximos controllers.

O Postman pode importar a URL `/v3/api-docs`. Uma collection local tambem esta disponivel em
`docs/postman/commerce-ai-api.postman_collection.json`.
Enviar uma mensagem pelo Swagger ou Postman chama o Gemini e pode consumir cotas do provedor.
Nenhuma chave precisa ser informada no Swagger; ela e configurada no backend.

Se `/swagger-ui.html` retornar 404, pare a aplicacao, recarregue o projeto Maven
no IntelliJ e inicie novamente. Uma execucao antiga pode nao incluir o starter
springdoc adicionado ao `pom.xml`. Teste tambem `/swagger-ui/index.html` e `/v3/api-docs`.
O caminho `/swagger-ui.html` redireciona para `/swagger-ui/index.html`; ambos sao
verificados por teste automatizado. Se os tres caminhos retornarem 404, confira
se o processo usa o projeto atual e se `springdoc.api-docs.enabled` ou
`springdoc.swagger-ui.enabled` foram desativados por configuracao externa.

## Testes e build

Com o JDK 21 configurado:

```powershell
./mvnw.cmd clean verify
```

Os testes usam credenciais ficticias e um modelo simulado, sem chamadas externas ou consumo de cotas.
Um teste adicional inicializa a configuracao real do Gemini sem enviar mensagens.
Isso verifica a integracao de componentes, mas nao comprova autenticacao e respostas da API externa.

## Validacao da Fase 1

A chamada real ao Gemini 3.5 Flash-Lite e os testes manuais pelo Swagger e Postman
foram confirmados pelo desenvolvedor em 14/10/2026. A Fase 1 esta concluida.
Logs temporarios de diagnostico foram removidos, incluindo dados da chave e stack traces diretos.
A Fase 2 inclui memoria conversacional temporaria implementada; frontend e streaming permanecem pendentes.

Se desejar manter o cache do Maven dentro do projeto:

```powershell
$env:MAVEN_USER_HOME = "$PWD/.build-cache"
./mvnw.cmd '-Dmaven.repo.local=.build-cache/repository' clean verify
```

## Conceitos desta etapa

- LLM (Large Language Model): modelo treinado para interpretar e gerar linguagem; pode produzir informacoes incorretas.
- Prompt de sistema: instrucoes sobre papel, idioma e limites do assistente, separadas da mensagem do usuario.
- ChatClient: API do Spring AI que monta o prompt e coordena a comunicacao com o modelo.
- Tokens: unidades de texto processadas pelo modelo; limitamos a saida a 1024 tokens.
- Memoria conversacional: janela de interacoes recentes que a API envia ao modelo para manter o contexto.
- TTL (Time To Live): periodo de inatividade apos o qual a sessao expira; nesta etapa, 30 minutos.
- Temperatura: parametro de variacao da geracao em modelos que o suportam; nao garante respostas verdadeiras.
  Removemos a temperatura personalizada do YAML. O Spring AI 2.0.1 ainda envia seus
  valores padrao de amostragem, que o Gemini 3.8 Flash ignora atualmente. Revisar a
  compatibilidade da biblioteca se o provedor passar a rejeitar esses parametros.

O prompt orienta o modelo a reconhecer a ausencia de dados comerciais. A garantia de respostas
fundamentadas dependera de consultas a fontes reais e avaliacoes nas proximas fases.

## Documentacao

- [Visao do produto](docs/PROJECT.md)
- [Arquitetura](docs/ARCHITECTURE.md)
- [Roadmap](docs/ROADMAP.md)
