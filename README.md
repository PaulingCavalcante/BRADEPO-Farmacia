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

#### Venda comum
```bash
curl -X POST http://localhost:8080/venda \
  -H "Content-Type: application/json" \
  -d "{\"cpf\":\"529.982.247-25\",\"produto\":\"Dipirona\"}"
```

#### Medicamento controlado (inclui protocolo ANS na resposta)
```bash
curl -X POST http://localhost:8080/venda \
  -H "Content-Type: application/json" \
  -d "{\"cpf\":\"529.982.247-25\",\"produto\":\"Diazepam\"}"
```

#### CPF inválido
```bash
curl -X POST http://localhost:8080/venda \
  -H "Content-Type: application/json" \
  -d "{\"cpf\":\"00000000000\",\"produto\":\"Dipirona\"}"
```

#### Produto sem estoque (Ritalina)
```bash
curl -X POST http://localhost:8080/venda \
  -H "Content-Type: application/json" \
  -d "{\"cpf\":\"529.982.247-25\",\"produto\":\"Ritalina\"}"
```

#### Listar notas emitidas
```bash
curl http://localhost:8080/notas
```

**Para encerrar**, volte ao terminal da aplicação e pressione `Ctrl + C`.
