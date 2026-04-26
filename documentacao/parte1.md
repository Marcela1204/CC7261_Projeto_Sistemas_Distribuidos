# Funcionalidades
## 🔹 Parte 1 – Req/Rep

### 🔐 Login de Usuário

* Cliente envia nome de usuário
* Servidor valida e responde:

  * sucesso ✅
  * erro ❌

### 📂 Criação de Canais

* Clientes criam canais
* Servidor valida duplicidade

### 📋 Listagem de Canais

* Retorno de todos os canais cadastrados

### 💾 Persistência

* Logins
* Canais criados

# Arquitetura
## 🔁 Req/Rep (Parte 1)

```mermaid
graph LR
Cliente --> Broker
Broker --> Servidor
```

* Cliente: REQ
* Servidor: REP
* Broker: ROUTER/DEALER

✔ Permite escalabilidade
✔ Balanceamento de carga
✔ Desacoplamento