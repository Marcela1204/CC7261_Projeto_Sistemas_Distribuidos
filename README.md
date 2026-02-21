## Introdução

A troca de mensagens instantâneas é um dos pilares da comunicação em sistemas distribuídos modernos. Soluções amplamente utilizadas atualmente, como sistemas de chat e mensagerias, têm suas origens em tecnologias mais antigas, como os Bulletin Board Systems (BBS) e o Internet Relay Chat (IRC), que permitiam a comunicação entre usuários por meio de servidores distribuídos.

Inspirado nesses sistemas clássicos, este projeto tem como objetivo o desenvolvimento de uma versão simplificada de um sistema de troca de mensagens instantâneas, aplicando os conceitos estudados na disciplina de **Sistemas Distribuídos**. A solução permite que usuários automatizados (bots) publiquem mensagens em canais públicos, com todas as interações sendo persistidas em disco, possibilitando a recuperação de mensagens anteriores.

O sistema foi projetado para operar de forma distribuída, permitindo a adição e remoção de servidores sem a interrupção do serviço. A comunicação entre clientes e servidores ocorre de forma assíncrona, utilizando troca de mensagens, e toda a execução do ambiente é automatizada por meio de containers e orquestração, sem a necessidade de interação manual do usuário final.

Dessa forma, o projeto busca consolidar, na prática, conceitos fundamentais como comunicação entre processos, interoperabilidade entre diferentes linguagens de programação, serialização de dados, persistência e execução distribuída.

---

## Decisões de Projeto e Tecnologias Utilizadas

Esta seção descreve as principais decisões técnicas adotadas no desenvolvimento do projeto, bem como as justificativas para cada escolha. Os campos destacados devem ser preenchidos de acordo com as decisões tomadas durante a implementação.

### Linguagens de Programação

**Linguagens utilizadas:**
- Cliente(s): ______________________________  
- Servidor(es): ______________________________  

**Justificativa da escolha:**

[Descreva aqui os motivos da escolha das linguagens, considerando critérios como familiaridade, suporte a sistemas distribuídos, bibliotecas disponíveis, interoperabilidade entre linguagens e facilidade de integração com o ZeroMQ.]

---

### Comunicação entre Serviços

**Biblioteca de troca de mensagens:**  
- ZeroMQ

**Padrão de comunicação adotado (ex.: PUB/SUB, REQ/REP, PUSH/PULL):**  
- ______________________________

**Justificativa da escolha:**

[Explique o motivo da escolha do padrão de comunicação, considerando aspectos como desacoplamento entre clientes e servidores, escalabilidade, simplicidade de implementação e aderência aos requisitos do projeto.]

---

### Serialização das Mensagens

**Formato de serialização utilizado:**  
- ______________________________ (ex.: JSON, MessagePack, Protobuf, etc.)

**Estrutura básica da mensagem:**  
- Campos incluídos: ______________________________  

[Descreva aqui a estrutura da mensagem trocada entre clientes e servidores.]

**Justificativa da escolha:**

[Justifique o formato de serialização escolhido, levando em conta legibilidade, eficiência, compatibilidade entre linguagens e facilidade de depuração.]

---

### Persistência dos Dados

**Forma de armazenamento dos dados:**  
- ______________________________ (ex.: arquivos em disco, banco de dados relacional, NoSQL, etc.)

**Dados persistidos:**  
- ______________________________  

**Justificativa da escolha:**

[Explique por que essa forma de persistência foi escolhida, considerando simplicidade, desempenho, facilidade de recuperação de mensagens e aderência aos requisitos do projeto.]

---

### Uso de Containers e Orquestração

**Ferramentas utilizadas:**
- Containers: ______________________________  
- Orquestração: ______________________________  

**Justificativa da escolha:**

[Descreva como o uso de containers e orquestração contribui para a padronização do ambiente, isolamento dos serviços, facilidade de execução e comunicação em rede entre os componentes do sistema.]

---

## Considerações Finais

[Espaço para comentar aprendizados obtidos durante o desenvolvimento, desafios enfrentados, limitações da solução e possíveis melhorias futuras.]
