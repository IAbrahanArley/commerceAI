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
O modelo padrao e `gemini-2.5-flash`; use `GEMINI_MODEL` para substitui-lo.
Verifique cotas e condicoes da modalidade gratuita no provedor antes de usar.
A aplicacao exige uma chave configurada para iniciar normalmente.

## Chat

`POST http://localhost:8080/api/v1/chat`

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
O identificador e devolvido para correlacao; nao existe memoria ou persistencia de conversas.

Erros usam `application/problem+json`: 400 para JSON ou campos invalidos, 429 para cota
do provedor, 502 para falha do provedor ou resposta vazia, 503 para indisponibilidade
ou falha de rede e 500 para erro inesperado. Detalhes internos nao sao devolvidos.
As chamadas possuem timeout de 30 segundos e uma tentativa por camada, sem repeticoes automaticas.

O endpoint ainda nao possui autenticacao ou limitacao local de chamadas; utilize-o em desenvolvimento local.
Nao ha consultas a produtos, estoque ou politicas comerciais nesta etapa.

## Testes e build

Com o JDK 21 configurado:

```powershell
./mvnw.cmd clean verify
```

Os testes usam credenciais ficticias e um modelo simulado, sem chamadas externas ou consumo de cotas.
Um teste adicional inicializa a configuracao real do Gemini sem enviar mensagens.
Isso verifica a integracao de componentes, mas nao comprova autenticacao e respostas da API externa.

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
- Temperatura: parametro de variacao da geracao, configurado em 0.2; nao garante respostas verdadeiras.

O prompt orienta o modelo a reconhecer a ausencia de dados comerciais. A garantia de respostas
fundamentadas dependera de consultas a fontes reais e avaliacoes nas proximas fases.

## Documentacao

- [Visao do produto](docs/PROJECT.md)
- [Arquitetura](docs/ARCHITECTURE.md)
- [Roadmap](docs/ROADMAP.md)
