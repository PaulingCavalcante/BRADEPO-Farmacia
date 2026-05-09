# BRADECO Farmácia

Sistema de vendas para farmácia com validação de CPF, emissão de NF-e (SEFAZ) e notificação de receitas controladas (ANS). Todos os serviços externos são simulados — sem banco de dados nem dependências de rede.

## Pré-requisitos

Verifique se Java e Maven estão instalados abrindo o terminal e executando:

```bash
java --version
mvn --version
```

Versões mínimas: **Java 17** e **Maven 3.8**.

## Como executar

**1. Compile o projeto** (na pasta raiz, onde está o `pom.xml`):

```bash
mvn clean install
```

Aguarde **BUILD SUCCESS**.

**2. Inicie a aplicação:**

```bash
cd farmacia-service
mvn spring-boot:run
```

Aguarde a mensagem `Started FarmaciaApplication`. A API estará em `http://localhost:8080`.

**3. Abra um novo terminal** e teste com os comandos abaixo.

## Testes

Use **curl** (terminal) ou um cliente como **Postman / Insomnia** com `POST http://localhost:8080/venda`.

---

#### Venda comum

```bash
curl -X POST http://localhost:8080/venda \
  -H "Content-Type: application/json" \
  -d "{\"cpf\":\"529.982.247-25\",\"produto\":\"Dipirona\"}"
```

```json
{ "cpf": "529.982.247-25", "produto": "Dipirona" }
```

Resposta esperada:
```json
{
  "status": "AUTORIZADA",
  "nota": { "id": "...", "cpf": "529.982.247-25", "produto": "Dipirona" },
  "protocoloSefaz": "SEFAZ-...",
  "protocoloAns": null,
  "motivo": null
}
```

---

#### Medicamento controlado (inclui protocolo ANS na resposta)

```bash
curl -X POST http://localhost:8080/venda \
  -H "Content-Type: application/json" \
  -d "{\"cpf\":\"529.982.247-25\",\"produto\":\"Diazepam\"}"
```

```json
{ "cpf": "529.982.247-25", "produto": "Diazepam" }
```

Resposta esperada:
```json
{
  "status": "AUTORIZADA",
  "nota": { "id": "...", "cpf": "529.982.247-25", "produto": "Diazepam" },
  "protocoloSefaz": "SEFAZ-...",
  "protocoloAns": "ANS-...",
  "motivo": null
}
```

---

#### CPF inválido

```bash
curl -X POST http://localhost:8080/venda \
  -H "Content-Type: application/json" \
  -d "{\"cpf\":\"00000000000\",\"produto\":\"Dipirona\"}"
```

```json
{ "cpf": "00000000000", "produto": "Dipirona" }
```

Resposta esperada:
```json
{
  "status": "NEGADA",
  "nota": null,
  "protocoloSefaz": null,
  "protocoloAns": null,
  "motivo": "CPF invalido"
}
```

---

#### Produto sem estoque (Ibuprofeno)

```bash
curl -X POST http://localhost:8080/venda \
  -H "Content-Type: application/json" \
  -d "{\"cpf\":\"529.982.247-25\",\"produto\":\"Ibuprofeno\"}"
```

```json
{ "cpf": "529.982.247-25", "produto": "Ibuprofeno" }
```

Resposta esperada:
```json
{
  "status": "NEGADA",
  "nota": null,
  "protocoloSefaz": null,
  "protocoloAns": null,
  "motivo": "produto sem estoque"
}
```

---

#### Produto vazio (requisição inválida)

```bash
curl -X POST http://localhost:8080/venda \
  -H "Content-Type: application/json" \
  -d "{\"cpf\":\"529.982.247-25\",\"produto\":\"\"}"
```

```json
{ "cpf": "529.982.247-25", "produto": "" }
```

Resposta esperada:
```json
{
  "status": "NEGADA",
  "nota": null,
  "protocoloSefaz": null,
  "protocoloAns": null,
  "motivo": "Requisição vazia"
}
```

---

#### Listar notas emitidas

```bash
curl http://localhost:8080/notas
```

Retorna a lista de todas as vendas autorizadas desde a última inicialização.

---

**Para encerrar**, volte ao terminal da aplicação e pressione `Ctrl + C`.
