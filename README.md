# base-project-spring-ai

API de chat com Java 25, Spring Boot, Spring AI e Ollama. Este projeto implementa o primeiro marco do plano: receber uma mensagem por HTTP, executar um caso de uso e retornar a resposta do modelo através de uma porta independente do provedor.

## Stack

| Componente | Versão |
| --- | --- |
| Java | 25 |
| Spring Boot | 4.1.1 |
| Spring AI | 2.0.1 |
| Gradle Wrapper | 9.8.0 |
| Lombok | 1.18.48 |
| ArchUnit | 1.5.1 |

As versões do Spring são releases estáveis, sem milestones ou snapshots. JUnit Jupiter, Mockito e AssertJ são gerenciados pelo BOM do Spring Boot. Lombok gera o construtor do controller; o domínio e os casos de uso continuam em Java puro.

## Arquitetura

```text
REST → AskAiUseCase → AskAiService → ChatModelPort
                                      ↑
                            OllamaChatModelAdapter → Spring AI → Ollama
```

- `domain`: value objects `Message` e `AiResponse`, com suas invariantes.
- `application`: portas, caso de uso e erro de provedor independente do framework.
- `adapter/in/rest`: validação HTTP, DTOs, mapper e controller.
- `adapter/out/ai/ollama`: integração com Spring AI e tradução de falhas externas.
- `infrastructure`: composição dos beans e tratamento de erros HTTP.

ArchUnit verifica que o domínio depende somente do próprio domínio e do Java, e que a aplicação depende somente de Java, domínio e aplicação. Os adapters não dependem da infraestrutura. Não há persistência, segurança, RAG ou entidades de conversação artificiais nesta etapa.

## Executar tudo com Docker Compose

Pré-requisito: Docker com Compose e acesso à Internet para baixar as imagens, dependências e o modelo na primeira execução. Não é necessário instalar Java ou Gradle no host.

```bash
docker compose up --build -d --wait --wait-timeout 600
```

O Compose faz todo o fluxo:

1. Compila e testa a aplicação em uma imagem de build com JDK 25 usando o Gradle Wrapper.
2. Inicia o Ollama e aguarda seu healthcheck.
3. Executa `ollama-init` para baixar `qwen3:4b`, reutilizando as camadas do modelo já presentes no volume.
4. Inicia a API somente quando a preparação do modelo terminar com sucesso e aguarda a saúde da aplicação.

A imagem final usa Java 25 JRE e executa a aplicação com usuário sem privilégios. O código-fonte, o Gradle e as ferramentas de compilação ficam na etapa de build. `.dockerignore` exclui arquivos locais, credenciais `.env`, caches e JARs compilados no host.

O primeiro download pode levar vários minutos; aumente `--wait-timeout` se necessário. Se o download do modelo falhar, a API não é iniciada. Verifique `docker compose logs ollama ollama-init`; corrija o acesso à rede e execute novamente o mesmo comando.

A imagem do Ollama está fixada por digest (versão 0.40.0 validada nesta implementação). Por padrão, o Compose usa CPU e mantém os modelos no volume `ollama-data`; a geração pode ser lenta sem GPU. Tanto a API quanto o Ollama são publicados somente no loopback do host.

```bash
docker compose ps -a
docker compose logs -f app
curl -f http://localhost:8080/actuator/health
curl -i http://localhost:8080/api/v1/chat \
  -H 'Content-Type: application/json' \
  -d '{"message":"Explain SOLID principles"}'
```

Resposta:

```json
{"answer":"SOLID is..."}
```

`/actuator/health` verifica a aplicação; uma chamada de chat verifica também a disponibilidade do modelo.

Para usar outro modelo ou outra porta no host:

```bash
OLLAMA_MODEL=llama3.2:3b SERVER_PORT=8081 docker compose up --build -d --wait --wait-timeout 600
```

O mesmo `OLLAMA_MODEL` é usado no download e na configuração da API. Dentro da rede Docker, a aplicação acessa `http://ollama:11434` e escuta sempre na porta 8080. `SERVER_PORT` controla a porta publicada no host.

Para parar os serviços mantendo os modelos:

```bash
docker compose down
```

`docker compose down -v` também remove o volume dos modelos.

## Desenvolvimento sem containerizar a API

Para executar Gradle no host, instale JDK 25 e configure `JAVA_HOME` e `PATH`. O Wrapper já está incluído no projeto.

```bash
docker compose up -d --wait ollama
docker compose run --rm ollama-init
./gradlew bootRun
```

Se já houver uma API do Compose usando a porta 8080, pare-a com `docker compose stop app` antes de iniciar `bootRun`. Como alternativa, use Ollama instalado diretamente: execute `ollama serve` e `ollama pull qwen3:4b`.

| Variável | Padrão da API no host | Uso |
| --- | --- | --- |
| `OLLAMA_BASE_URL` | `http://localhost:11434` | Endereço do Ollama |
| `OLLAMA_MODEL` | `qwen3:4b` | Modelo previamente baixado |
| `SERVER_PORT` | `8080` | Porta HTTP da aplicação |

Para um Ollama remoto, configure `OLLAMA_BASE_URL` ao executar a API no host. A aplicação não implementa autenticação ou autorização nesta primeira etapa.

## Contrato HTTP

`POST /api/v1/chat`, com JSON `{"message":"..."}`:

- `200`: JSON com `answer`.
- `400`: mensagem ausente, nula, em branco, com mais de 16.000 caracteres ou JSON inválido.
- `502`: falha do provedor ou resposta vazia; detalhes internos não são expostos.

Erros incluem `timestamp`, `status`, `message` e `details`. O cliente HTTP tem timeout de conexão de 5 segundos e leitura de 120 segundos. Não há retry automático de geração.

## Build e testes

```bash
./gradlew build
./gradlew test
java -jar build/libs/base-project-spring-ai-0.0.1-SNAPSHOT.jar
```

A suíte padrão não precisa de Docker, GPU nem modelo instalado. Ela cobre invariantes, caso de uso com Mockito, validação e erros HTTP com MockMvc, regras de arquitetura e integração da aplicação completa com o cliente real do Spring AI contra um servidor HTTP de teste que implementa o contrato do Ollama. Esse teste verifica o transporte e o mapeamento; não avalia a qualidade de uma inferência real.

Relatório de testes: `build/reports/tests/test/index.html`.

## Ambiente cloud

Nesta máquina, ative o JDK 25 e a configuração de proxy antes do Gradle:

```bash
source /workspace/.setup/activate-java25.sh
cd /workspace/base-project-spring-ai
./gradlew build --no-daemon --max-workers=2
```

Para executar o Compose neste cloud, o helper de instalação também prepara uma configuração externa para o proxy e o truststore Java:

```bash
source /workspace/.setup/activate-java25.sh
python /workspace/.setup/prepare-compose-cloud.py
docker compose -f docker-compose.yml -f /workspace/.setup/compose-cloud.yml \
  up --build -d --wait --wait-timeout 600
```

O override é específico deste ambiente; não é necessário no Docker local. As opções Java e o truststore são passados como secrets temporários do BuildKit e não são incluídos na imagem final. A política de rede continua sendo respeitada. Este cloud usa o driver Docker `vfs`, que pode exigir bastante espaço adicional por copiar camadas para cada container.

Para baixar o modelo no cloud, permita `registry.ollama.ai` na política de rede; eventuais destinos CDN adicionais devem ser confirmados ao executar o download.

A configuração de rede do cloud fica fora do repositório e utiliza o truststore do sistema, mantendo a verificação TLS. Cada tarefa já tem seu checkout isolado; não crie worktrees adicionais para executar este projeto.

## Próximos marcos

1. Prompts de sistema, templates e saída estruturada.
2. Tool calling.
3. RAG, seguido de PostgreSQL e pgvector quando houver um caso de uso.
4. Agentes, MCP e outros provedores através de novas implementações de `ChatModelPort`.
