# Funcionalidades

### 1. Eleição de Coordenador
- Servidores elegem um **coordenador** único entre eles
- Critério: **menor nome** (ordem alfabética)
- Se o coordenador falhar, uma nova eleição é iniciada

### 2. Sincronização de Relógio (Algoritmo de Berkeley)
- Servidores requisitam a hora ao coordenador
- Coordenador responde com seu timestamp
- Servidor calcula **RTT** (Round Trip Time)
- Ajusta seu relógio com base na hora do coordenador
- Sincronização ocorre a cada **15 mensagens** processadas

### 3. Heartbeat Simplificado
- Servidor envia heartbeat ao Reference Server
- Reference Server responde apenas **"OK"** (sem hora)
- A hora agora é fornecida exclusivamente pelo coordenador

### 4. Publicação do Coordenador
- Quando um novo coordenador é eleito, ele publica no tópico `servers`
- Todos os servidores atualizam sua referência de coordenador