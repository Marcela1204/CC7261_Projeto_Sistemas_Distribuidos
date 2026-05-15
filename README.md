<div align="center">

# PROJETO DE SISTEMAS DISTRIBUÍDOS
</div>

## 📖 Descrição

Este projeto implementa um sistema distribuído de troca de mensagens inspirado em sistemas clássicos como BBS (Bulletin Board System) e IRC (Internet Relay Chat). O sistema permite que múltiplos clientes (bots) se conectem a servidores, realizem login, criem canais, listem canais disponíveis e publiquem mensagens em canais — tudo de forma distribuída, escalável e automatizada.

A comunicação entre os serviços é feita utilizando **ZeroMQ**, garantindo eficiência, baixo acoplamento e interoperabilidade entre **Java (JeroMQ)** e **Python (PyZMQ)**.

---

## 🚀 Funcionalidades Implementadas

O projeto foi desenvolvido em cinco partes incrementais, cada uma adicionando novas funcionalidades e complexidades ao sistema.

### Parte 1: Comunicação Básica Req/Rep
- **Login de Usuário**: Clientes enviam nome de usuário para servidores via REQ/REP. Servidores validam e respondem com sucesso ou erro.
- **Criação de Canais**: Clientes podem criar novos canais, com validação de duplicidade pelos servidores.
- **Listagem de Canais**: Servidores retornam lista de todos os canais cadastrados.
- **Persistência Básica**: Logs de logins e canais criados são armazenados em arquivos locais.

### Parte 2: Publicação e Subscrição Pub/Sub
- **Publicação em Canais**: Clientes solicitam publicação ao servidor via REQ, que publica no canal correspondente via PUB/SUB.
- **Inscrição em Canais**: Clientes se inscrevem em múltiplos canais e recebem mensagens automaticamente.
- **Controle de Tempo**: Cada mensagem inclui timestamp de envio (servidor) e recebimento (cliente).
- **Persistência Avançada**: Publicações armazenadas em disco; requisições registradas para auditoria completa.

### Parte 3: Relógio Lógico e Sincronização
- **Relógio Lógico de Lamport**: Implementado em clientes e servidores para ordenação de eventos. Incrementa contador antes de enviar, atualiza com max(local, recebido) ao receber.
- **Sincronização de Relógio Físico**: Serviço de referência fornece tempo atual baseado em `System.currentTimeMillis()` (Java) ou `time.time()` (Python).
- **Serviço de Referência**: Atribui ranks aos servidores, mantém lista de ativos, fornece tempo e recebe heartbeats.
- **Heartbeat**: Servidores enviam heartbeat a cada 10 mensagens processadas para informar atividade e atualizar relógio.
- **Detecção de Falhas**: Referência remove servidores inativos da lista.

### Parte 4: Eleição de Coordenador e Sincronização Berkeley
- **Eleição de Coordenador**: Servidores elegem coordenador com menor nome alfabético. Nova eleição se falhar.
- **Sincronização de Relógio (Berkeley)**: Servidores requisitam hora ao coordenador, calculam RTT e ajustam relógio a cada 15 mensagens.
- **Heartbeat Simplificado**: Envio ao Reference Server sem hora; hora fornecida pelo coordenador.
- **Publicação do Coordenador**: Novo coordenador publica no tópico `servers` para atualização de todos.

### Parte 5: [A ser definido - documentação incompleta]

---

## 🔄 Fluxos de Comunicação

Os fluxos incluem sequências para login, criação/listagem de canais, publicação, registro de servidores, listagem, heartbeat, eleição e sincronização Berkeley. Diagramas Mermaid detalham cada interação.

---

## 🤖 Comportamento dos Bots

Clientes implementados como bots automáticos:
1. Login com usuário aleatório.
2. Criam canais se < 5 existirem.
3. Inscrevem-se em até 3 canais.
4. Loop: Escolhem canal aleatório, enviam 10 mensagens (1s intervalo), recebem mensagens inscritas.

Simula ambiente real para testes e validação.

---

## 💾 Armazenamento de Dados

Persistência em arquivos locais:
- **Publicações**: `canal;mensagem;timestamp`
- **Requisições**: `REQ;conteudo;timestamp`

Justificativa: Histórico completo, auditoria, recuperação futura.

---

## ⚙️ Decisões de Implementação

### Tecnologias e Linguagens
- **ZeroMQ (JeroMQ/PyZMQ)**: Comunicação assíncrona leve, suporte a múltiplas linguagens, padrões Req/Rep e Pub/Sub.
- **Java e Python**: Demonstra interoperabilidade; Java para performance, Python para prototipagem rápida.
- **Docker/Docker Compose**: Containerização para isolamento e orquestração fácil.

### Arquitetura
- **Broker (ROUTER/DEALER)**: Permite múltiplos servidores, balanceamento automático.
- **Proxy Pub/Sub Separado**: Isola responsabilidades, evita mistura de padrões.
- **Tópicos como Canais**: Aproveita filtragem nativa do ZeroMQ.
- **Servidor como Intermediador**: Centraliza validação, consistência, persistência.
- **Interoperabilidade Java + Python**: Compatibilidade demonstrada, facilita testes.

### Algoritmos
- **Relógio Lógico de Lamport**: Ordenação causal de eventos em sistema distribuído.
- **Sincronização Berkeley**: Ajuste de relógios com medição de RTT.
- **Eleição de Coordenador**: Simples, baseada em nome alfabético.
- **Heartbeat**: Detecção de falhas periódica.

### Persistência
- Arquivos locais para simplicidade; em produção, usaria banco de dados distribuído.
- Sincronização de canais entre servidores via Reference Server.

### Implementação em Java e Python
- **Estrutura Similar**: Classes como `LogicalClock`, `Servidor`, `Cliente` em ambas linguagens.
- **ZeroMQ Bindings**: JeroMQ para Java, PyZMQ para Python; APIs similares facilitam tradução.
- **Tratamento de Exceções**: Try-catch em Java, try-except em Python.
- **Threads**: `Thread` em Java, `threading` em Python para operações concorrentes.
- **I/O**: `Files` e `BufferedWriter` em Java, `open` e context managers em Python.
- **Decisões Específicas**: Uso de `ArrayList`/`List` em Java vs. listas em Python; `ZMQ.Context` vs. `zmq.Context`.

---

## 🛠️ Tecnologias Utilizadas

- ZeroMQ (JeroMQ/PyZMQ): Comunicação assíncrona e leve
- Python e Java: Linguagens principais
- Docker / Docker Compose: Containerização e orquestração
- Mermaid: Diagramas de sequência

---

## ▶️ Como Executar

Na pasta do projeto:

```bash
docker compose up
```

Isso inicia todos os serviços: brokers, proxies, servidores, clientes e referência.
