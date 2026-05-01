## Login

```mermaid
sequenceDiagram
  Cliente ->> Servidor: REQ (logar usuario)
  Servidor ->> Cliente: REP (sucesso/erro)
```

## Criar canal

```mermaid
sequenceDiagram
  Cliente ->> Servidor: REQ (adiciona canal)
  Servidor ->> Cliente: REP (resultado)
```

## Listar canais

```mermaid
sequenceDiagram
  Cliente ->> Servidor: REQ (lista)
  Servidor ->> Cliente: REP (canais)
```

## Publicação (Parte 2)

```mermaid
sequenceDiagram
  Cliente ->> Servidor: REQ (publica canal mensagem)
  Servidor ->> Proxy: PUB (mensagem)
  Servidor ->> Cliente: REP (OK/erro)
```

## Registro de servidor (Parte 3)

```mermaid
sequenceDiagram
  Servidor ->> Referência: REGISTER nome
  Referência ->> Servidor: RANK id
```

---

## Listagem de servidores (Parte 3)

```mermaid
sequenceDiagram
  Servidor ->> Referência: LIST
  Referência ->> Servidor: nome:rank,...
```

---

## Heartbeat (Parte 3)

```mermaid
sequenceDiagram
  Servidor ->> Referência: HEARTBEAT nome
  Referência ->> Servidor: OK timestamp
```

## Eleição (Parte 4)
```mermaid
sequenceDiagram
    Servidor A->>Servidor B: REQ|eleicao
    Servidor B-->>Servidor A: REP|OK
    Servidor A->>Servidor A: Escolhe coordenador
    Servidor A->>Proxy: PUB servers coordenador
```

## Sincronização Berkeley (Parte 4)
```mermaid
sequenceDiagram
    sequenceDiagram
    Servidor->>Coordenador: REQ_HORA
    Coordenador-->>Servidor: REP_HORA|timestamp
    Servidor->>Servidor: Calcula RTT e ajusta clock
```