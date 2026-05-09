# BRADECO Farmácia — Documentação do Projeto

## Visão Geral

O **BRADECO Farmácia** é um sistema de vendas farmacêuticas construído com **Java 17** e **Spring Boot 3.2.5**, organizado como um projeto **Maven multi-módulo**. Ele simula o fluxo completo de uma venda em farmácia: validação do CPF do cliente, verificação de estoque junto ao fornecedor, emissão de Nota Fiscal Eletrônica (NF-e) via SEFAZ e notificação da ANS para medicamentos controlados.

Todas as integrações externas (SEFAZ, ANS, Fornecedor) são **simuladas em memória** — não há banco de dados nem chamadas HTTP reais.

---

## Estrutura de Pastas

```
BRADECO-Farmacia/
├── .claude/                     # Configurações locais do Claude Code (não versionar segredos)
├── .github/                     # Scripts de automação de CI/CD (ex: upgrade de Java)
│   └── java-upgrade/
├── farmacia-service/            # Módulo principal — aplicação Spring Boot
├── cpf-validator-component/     # Módulo — validação de CPF
├── sefaz-component/             # Módulo — emissão de NF-e (integração SEFAZ)
├── ans-component/               # Módulo — notificação de medicamentos controlados (ANS)
├── fornecedor-component/        # Módulo — adaptador para o sistema legado do fornecedor
└── pom.xml                      # POM raiz (agrega todos os módulos)
```

---

## Para Que Serve Cada Pasta

### `farmacia-service/`
**O orquestrador central.** É o único módulo que sobe como aplicação Spring Boot. Recebe as requisições REST, executa as regras de negócio e persiste os dados em memória.

```
farmacia-service/
└── src/main/
    ├── java/com/farmacia/
    │   ├── FarmaciaApplication.java          ← Ponto de entrada Spring Boot
    │   ├── controller/
    │   │   └── VendaController.java          ← Endpoints REST (POST /venda, GET /notas)
    │   ├── service/
    │   │   └── VendaService.java             ← Orquestração das regras de negócio
    │   ├── repository/
    │   │   └── VendaRepository.java          ← Armazenamento em memória
    │   └── dto/
    │       ├── VendaRequest.java             ← Corpo da requisição POST /venda
    │       └── VendaResponse.java            ← Corpo das respostas da API
    └── resources/
        └── application.properties            ← Configuração da porta (8080)
```

- **`FarmaciaApplication.java`** — Contém apenas o `main()`. Ponto de entrada do Spring Boot.
- **`VendaController.java`** — Recebe as requisições HTTP e delega para o `VendaService`.
- **`VendaService.java`** — Orquestra CPF, estoque, SEFAZ e ANS para processar a venda.
- **`VendaRepository.java`** — Mantém a lista de notas autorizadas em memória (`List<VendaResponse>`).
- **`VendaRequest.java`** — DTO de entrada: campos `cpf` e `produto`.
- **`VendaResponse.java`** — DTO de saída: campos `status`, `nota`, `protocoloSefaz`, `protocoloAns`, `motivo`.

---

### `cpf-validator-component/`
**Validação do CPF do cliente.** Isola a lógica de validação num componente reutilizável, separada do serviço principal.

```
cpf-validator-component/
└── src/main/java/com/farmacia/componentes/cpf/
    ├── CpfValidator.java        ← Interface pública
    └── CpfValidatorImpl.java    ← Implementação com algoritmo módulo 11
```

- Remove caracteres não numéricos.
- Verifica se possui exatamente 11 dígitos.
- Rejeita sequências repetidas (ex: `00000000000`).
- Calcula e confere os dois dígitos verificadores pelo algoritmo módulo 11.

---

### `sefaz-component/`
**Emissão de Nota Fiscal Eletrônica.** Simula a comunicação com a SEFAZ (Secretaria da Fazenda) para autorizar a NF-e de cada venda.

```
sefaz-component/
└── src/main/java/com/farmacia/componentes/sefaz/
    ├── SefazClient.java         ← Interface pública
    ├── SefazClientImpl.java     ← Implementação simulada
    └── NotaFiscal.java          ← Record com os dados da nota (id, cpf, produto)
```

- Gera um protocolo único no formato `SEFAZ-{timestamp}`.
- Loga no console: `[SEFAZ] Nota {id} autorizada -> {protocolo}`.

---

### `ans-component/`
**Notificação de medicamentos controlados.** Simula o registro na ANS (Agência Nacional de Saúde) quando o produto vendido exige receita controlada.

```
ans-component/
└── src/main/java/com/farmacia/componentes/ans/
    ├── AnsClient.java           ← Interface pública
    └── AnsClientImpl.java       ← Implementação simulada
```

- Ativado apenas para: **Rivotril**, **Diazepam** e **Ritalina** (case-insensitive).
- Gera protocolo no formato `ANS-{timestamp}`.
- Loga no console: `[ANS] Receita registrada (cpf=..., produto=...) -> {protocolo}`.

---

### `fornecedor-component/`
**Adaptador para o sistema legado do fornecedor.** Verifica a disponibilidade do produto em estoque, representando a integração com um sistema externo legado.

```
fornecedor-component/
└── src/main/java/com/farmacia/componentes/fornecedor/
    ├── FornecedorAdapter.java        ← Interface pública
    └── FornecedorAdapterImpl.java    ← Implementação com lista fixa de produtos
```

- Mantém uma lista fixa de produtos **sem estoque** (ex: Ibuprofeno).
- Grava cada consulta num arquivo de spool em: `{java.io.tmpdir}/fornecedor-legado.log`.
  - Formato da linha: `LocalDateTime|produto|OK/SEM_ESTOQUE`

---

### `.github/java-upgrade/`
Scripts de automação para atualização da versão do Java no projeto. Não faz parte do fluxo de negócio da aplicação.

### `.claude/`
Configurações locais do Claude Code para o projeto. Não afeta a aplicação em tempo de execução.

---

## Fluxo Completo: Do Início ao Fim

### 1. Inicialização

```bash
# Na raiz do projeto — compila todos os módulos
mvn clean install

# Sobe a aplicação
cd farmacia-service
mvn spring-boot:run
```

O Spring Boot inicializa e injeta automaticamente via construtor:
- `CpfValidatorImpl`
- `FornecedorAdapterImpl`
- `SefazClientImpl`
- `AnsClientImpl`

A aplicação fica disponível em `http://localhost:8080`.

---

### 2. Fluxo de uma Venda — `POST /venda`

```
Cliente envia: POST /venda
Body: { "cpf": "529.982.247-25", "produto": "Diazepam" }
```

```
┌──────────────────────────────────────────────────────────────┐
│  VendaController                                             │
│  └─► VendaService.processar(VendaRequest)                    │
│                                                              │
│  1. Validar CPF ───────────────► CpfValidatorImpl            │
│     └─ CPF inválido?                                         │
│        └─► VendaResponse(status=NEGADA, motivo=CPF invalido) │
│                                                              │
│  2. Verificar estoque ─────────► FornecedorAdapterImpl       │
│     └─ Sem estoque?                                          │
│        └─► VendaResponse(status=NEGADA, motivo=sem estoque)  │
│                                                              │
│  3. Criar NotaFiscal ──────────► NotaFiscal(UUID, ...)       │
│                                                              │
│  4. Emitir NF-e ───────────────► SefazClientImpl             │
│     └─► protocoloSefaz: "SEFAZ-{timestamp}"                  │
│                                                              │
│  5. Produto controlado? ───────► AnsClientImpl               │
│     (rivotril/diazepam/ritalina)                             │
│     └─► protocoloAns: "ANS-{timestamp}"                      │
│                                                              │
│  6. Salvar VendaResponse em VendaRepository                  │
│                                                              │
│  7. Retornar VendaResponse ◄──────────────────────────────── │
└──────────────────────────────────────────────────────────────┘
```

**Resposta de sucesso (produto comum):**
```json
{
  "status": "AUTORIZADA",
  "nota": { "id": "uuid", "cpf": "529.982.247-25", "produto": "Dipirona" },
  "protocoloSefaz": "SEFAZ-1714123456789",
  "protocoloAns": null,
  "motivo": null
}
```

**Resposta de sucesso (medicamento controlado):**
```json
{
  "status": "AUTORIZADA",
  "nota": { "id": "uuid", "cpf": "529.982.247-25", "produto": "Diazepam" },
  "protocoloSefaz": "SEFAZ-1714123456789",
  "protocoloAns": "ANS-1714123456790",
  "motivo": null
}
```

**Resposta de venda negada:**
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

### 3. Consulta de Notas — `GET /notas`

Retorna todas as notas autorizadas armazenadas em memória desde a última inicialização.

```bash
curl http://localhost:8080/notas
```

> **Atenção:** os dados são **voláteis** — ao reiniciar a aplicação, todas as notas são perdidas.

---

## Exemplos de Uso

```bash
# Venda aprovada — produto comum
curl -X POST http://localhost:8080/venda \
  -H "Content-Type: application/json" \
  -d '{"cpf":"529.982.247-25","produto":"Dipirona"}'

# Venda aprovada — medicamento controlado (aciona ANS)
curl -X POST http://localhost:8080/venda \
  -H "Content-Type: application/json" \
  -d '{"cpf":"529.982.247-25","produto":"Diazepam"}'

# CPF inválido
curl -X POST http://localhost:8080/venda \
  -H "Content-Type: application/json" \
  -d '{"cpf":"00000000000","produto":"Dipirona"}'

# Produto sem estoque
curl -X POST http://localhost:8080/venda \
  -H "Content-Type: application/json" \
  -d '{"cpf":"529.982.247-25","produto":"Ibuprofeno"}'

# Listar todas as notas
curl http://localhost:8080/notas
```

---

## Resumo dos Módulos Maven

| Módulo | Artefato | Versão | Tipo |
|---|---|---|---|
| `BRADECO-Farmacia` | pom raiz | 1.0.0 | pom |
| `farmacia-service` | farmacia-service | 1.0.0 | jar executável |
| `cpf-validator-component` | cpf-validator-component | 1.0.0 | jar |
| `sefaz-component` | sefaz-component | 1.0.0 | jar |
| `ans-component` | ans-component | 1.0.0 | jar |
| `fornecedor-component` | fornecedor-component | 1.0.0 | jar |

---

## Observações Importantes

- **Sem banco de dados:** todos os dados vivem em memória e são perdidos ao reiniciar.
- **Integrações simuladas:** SEFAZ, ANS e Fornecedor não fazem chamadas HTTP reais.
- **Log físico:** o `fornecedor-component` grava em `{tmp}/fornecedor-legado.log` para simular auditoria de sistema legado.
- **Porta padrão:** `8080`, configurável em `farmacia-service/src/main/resources/application.properties`.
