# 🧩 Parte 3 — Relógios e Heartbeat
# Funcionalidades
## 🧠 Relógio Lógico (Lamport)

O relógio lógico foi implementado em **clientes e servidores** para manter a ordem dos eventos no sistema.

### 🔹 Regras implementadas

* Antes de enviar uma mensagem:

  * O contador lógico é incrementado
* Ao receber uma mensagem:

  * O relógio é atualizado com:

```text
clock = max(clock_local, clock_recebido)
```

* Todas as mensagens carregam o valor do relógio

---

## ⏱️ Sincronização de Relógio Físico

Foi criado um **serviço de referência** responsável por fornecer o tempo atual para os servidores.

* Baseado em:

```java
System.currentTimeMillis()
```

* Utilizado para:

  * Atualizar o relógio físico dos servidores
  * Padronizar timestamps

---

## 🧩 Serviço de Referência

Novo componente adicionado ao sistema.

### 🔹 Responsabilidades

* Atribuir **rank** aos servidores
* Manter lista de servidores ativos
* Fornecer lista de servidores disponíveis
* Receber heartbeat
* Fornecer tempo atual

---

## ❤️ Heartbeat

Cada servidor envia heartbeat periodicamente:

* Frequência: a cada **10 mensagens processadas**

### 🔹 Funções do heartbeat

* Informar que o servidor está ativo
* Atualizar seu relógio físico
* Manter-se na lista de servidores disponíveis

---

## ⚠️ Detecção de falhas

* O serviço de referência remove servidores que:

  * Não enviam heartbeat dentro de um tempo limite

✔ Garante lista atualizada de servidores ativos
✔ Permite tolerância a falhas

---

## Arquitetura

Foi adicionado um novo componente:

* **Reference Server**

Fluxo atualizado:

```text
Servidor ↔ Reference Server
Servidor ↔ Broker ↔ Cliente
Servidor → Proxy → Clientes
```
