# BRADEPO Farmácia — Documentação do Projeto (v2)

## Visão Geral

O **BRADEPO Farmácia** é um sistema de vendas farmacêuticas em **Java 17** e
**Spring Boot 3.2.5**, organizado como projeto **Maven multi-módulo** com
**arquitetura baseada em componentes**. Cobre o fluxo de venda de ponta a ponta:
validação do CPF, consulta de disponibilidade no fornecedor, emissão de NF-e
(SEFAZ), notificação à ANS para controlados, cálculo de **desconto** e de
**comissão**, baixa de estoque e **persistência em MySQL**.

As integrações externas (SEFAZ, ANS, Fornecedor) são **simuladas** dentro de
componentes-jar reutilizáveis. A persistência (JPA/MySQL) vive apenas no
`farmacia-service`; os componentes não conhecem JPA.

---

## Estrutura de Pastas

```
BRADEPO-Farmacia/
├── cpf-validator-component/     # Componente — validação de CPF
├── sefaz-component/             # Componente — emissão de NF-e (SEFAZ)
├── ans-component/               # Componente — notificação de controlados (ANS)
├── fornecedor-component/        # Componente — disponibilidade no fornecedor (legado)
├── desconto-component/          # Componente — cálculo de desconto
├── farmacia-service/            # Aplicação Spring Boot (orquestrador + REST + JPA)
├── db/                          # Banco populado: dump + instruções
│   ├── farmacia-dump.sql
│   └── README.md
├── demo/                        # Roteiro de demonstração para o vídeo
│   └── roteiro.ps1
└── pom.xml                      # POM raiz (agrega os módulos)
```

---

## Componentes (jars reutilizáveis)

Cada componente expõe uma **interface** e uma **implementação** (`@Component`),
em pacotes `com.farmacia.componentes.*`. São jars puros que declaram apenas
`spring-context` (`spring.version=6.1.6`), sem Spring Boot.

### `cpf-validator-component`
Validação de CPF (algoritmo módulo 11): limpa não-dígitos, exige 11 dígitos,
rejeita sequências repetidas e confere os dois dígitos verificadores.
Reutilizado tanto na venda quanto no cadastro de clientes.

### `sefaz-component`
Emissão simulada de NF-e. `NotaFiscal(id, cpf, produto)` é um `record`; o
`SefazClient` gera um protocolo `SEFAZ-{timestamp}`.

### `ans-component`
Notificação simulada à ANS para medicamentos controlados; gera `ANS-{timestamp}`.

### `fornecedor-component`
Adaptador do sistema legado do fornecedor: indica disponibilidade e grava um
spool em `{java.io.tmpdir}/fornecedor-legado.log`.

### `desconto-component`  *(novo no Projeto 2)*
Cálculo de desconto sem dependência do domínio nem de JPA — recebe um
`DescontoContexto(valorBruto, clienteCadastrado, idoso, convenio)` e devolve um
`DescontoResultado(percentual, valorDesconto, valorLiquido, descricao)`.

Regras:
- Cliente **não** cadastrado: sem desconto.
- Cliente cadastrado: desconto **progressivo** do fabricante por faixa de valor
  (5% até R$50; 8% de R$50 a R$150; 12% acima de R$150).
- Idoso **com** convênio: também recebe o desconto de convênio (15%); aplica-se
  a **maior vantagem** entre convênio e fabricante (conforme enunciado).

---

## `farmacia-service` (orquestrador)

Único módulo que sobe como aplicação Spring Boot. Injeção sempre por construtor.

```
farmacia-service/src/main/
├── java/com/farmacia/
│   ├── FarmaciaApplication.java        ← main() Spring Boot
│   ├── controller/
│   │   ├── VendaController.java        ← POST /venda, GET /notas
│   │   ├── ProdutoController.java      ← CRUD /produtos
│   │   ├── ClienteController.java      ← CRUD /clientes
│   │   └── RelatorioController.java    ← /relatorios/*
│   ├── service/
│   │   ├── VendaService.java           ← orquestra a venda
│   │   ├── ProdutoService.java         ← regras de produto
│   │   ├── ClienteService.java         ← regras de cliente (reusa CpfValidator)
│   │   └── RelatorioService.java       ← relatórios
│   ├── repository/                     ← interfaces Spring Data JPA
│   │   ├── VendaRepository.java
│   │   ├── ProdutoRepository.java
│   │   └── ClienteRepository.java
│   ├── model/                          ← entidades JPA + enums
│   │   ├── Venda.java   Produto.java   Cliente.java
│   │   └── Categoria.java   Canal.java
│   └── dto/                            ← records de entrada/saída
└── resources/
    ├── application.properties          ← datasource MySQL + JPA + seed
    └── data.sql                        ← seed de produtos e clientes
```

### Entidades (tabelas)

**`produto`** — `id`, `nome` (único), `categoria` (`MEDICAMENTO`|`HIGIENE`),
`preco`, `estoque`, `controlado`. A flag `controlado` é a **fonte única** sobre
quais produtos exigem ANS (substituiu a antiga lista fixa no código).

**`cliente`** — `id`, `cpf` (único), `nome`, `idoso`, `convenio`. O cadastro
habilita os descontos.

**`venda`** — `id`, `status`, `cpf`, `produto`, `nota_id`, `protocolo_sefaz`,
`protocolo_ans`, `motivo`, `data_hora`, `valor_bruto`, `percentual_desconto`,
`valor_desconto`, `valor_liquido`, `descricao_desconto`, `canal`, `vendedor`,
`comissao`. Só vendas **AUTORIZADAS** são gravadas.

---

## Fluxo de uma Venda — `POST /venda`

Corpo: `{ "cpf": "...", "produto": "...", "canal": "INTERNET|BALCAO", "vendedor": "..." }`

```
VendaController → VendaService.processar(VendaRequest)

 1. Produto vazio?                         → NEGADA "Requisição vazia"
 2. Canal: INTERNET (padrão) ou BALCAO     → BALCAO sem vendedor: NEGADA
 3. Produto cadastrado?  (ProdutoRepository)→ não: NEGADA "produto nao cadastrado"
 4. Regra de CPF (Fase 3):
      - controlado  → CPF obrigatório e válido (CpfValidator)
      - comum       → CPF opcional; se vier, valida
 5. Disponível no fornecedor? (FornecedorAdapter) → não: NEGADA
 6. Estoque local > 0?                      → não: NEGADA "produto sem estoque"
 7. Emite NF-e (SefazClient)                → protocoloSefaz
 8. Controlado? (AnsClient)                 → protocoloAns
 9. Desconto (CalculadoraDesconto):
      - cadastrado? idoso? convenio? → percentual e valor
10. Comissão (se BALCAO): 5% do valor líquido
11. Baixa estoque (ProdutoRepository.save)
12. Persiste a Venda (VendaRepository.save)
13. Retorna VendaResponse (AUTORIZADA, nota, protocolos, canal, vendedor, financeiro)
```

**Resposta autorizada (resumida):**
```json
{
  "status": "AUTORIZADA",
  "nota": { "id": "uuid", "cpf": "529.982.247-25", "produto": "Dipirona" },
  "protocoloSefaz": "SEFAZ-...",
  "protocoloAns": null,
  "motivo": null,
  "canal": "INTERNET",
  "vendedor": null,
  "financeiro": {
    "valorBruto": 12.90,
    "percentualDesconto": 15.00,
    "valorDesconto": 1.94,
    "valorLiquido": 10.96,
    "descricaoDesconto": "Desconto convenio (idoso): 15% (maior vantagem)",
    "comissao": null
  }
}
```

---

## Endpoints

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/venda` | Processa uma venda |
| `GET` | `/notas` | Lista vendas autorizadas (do MySQL) |
| `POST` | `/produtos` | Cadastra produto |
| `GET` | `/produtos` / `/produtos/{id}` | Lista / busca produto |
| `PUT` | `/produtos/{id}` | Atualiza produto |
| `DELETE` | `/produtos/{id}` | Remove produto |
| `POST` | `/clientes` | Cadastra cliente |
| `GET` | `/clientes` / `/clientes/{id}` | Lista / busca cliente |
| `PUT` | `/clientes/{id}` | Atualiza cliente |
| `DELETE` | `/clientes/{id}` | Remove cliente |
| `GET` | `/relatorios/vendas?inicio=&fim=` | Vendas por período (datas ISO) |
| `GET` | `/relatorios/mais-vendidos` | Ranking por quantidade vendida |

---

## Como Executar

```powershell
# Credenciais do MySQL por variável de ambiente (nunca commitadas)
$env:DB_USER = "root"
$env:DB_PASS = "SUA_SENHA_MYSQL"

mvn clean install                 # compila os 6 módulos
cd farmacia-service
mvn spring-boot:run               # sobe na porta 8080
```

No primeiro boot, o Hibernate cria as tabelas (`ddl-auto=update`) e o `data.sql`
popula produtos e clientes. Para o banco já com vendas, importe
[`db/farmacia-dump.sql`](db/farmacia-dump.sql).

Roteiro completo da demo: [`demo/roteiro.ps1`](demo/roteiro.ps1).

---

## Configuração relevante (`application.properties`)

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/farmacia?createDatabaseIfNotExist=true&...
spring.datasource.username=${DB_USER:root}
spring.datasource.password=${DB_PASS:root}
spring.jpa.hibernate.ddl-auto=update
spring.sql.init.mode=always                    # roda data.sql também no MySQL
spring.jpa.defer-datasource-initialization=true # data.sql após o Hibernate criar tabelas
```

---

## Resumo dos Módulos Maven

| Módulo | Artefato | Versão | Tipo |
|---|---|---|---|
| `bradeco-farmacia-parent` | pom raiz | 1.0.0 | pom |
| `farmacia-service` | farmacia-service | 1.0.0 | jar executável |
| `cpf-validator-component` | cpf-validator-component | 1.0.0 | jar |
| `sefaz-component` | sefaz-component | 1.0.0 | jar |
| `ans-component` | ans-component | 1.0.0 | jar |
| `fornecedor-component` | fornecedor-component | 1.0.0 | jar |
| `desconto-component` | desconto-component | 1.0.0 | jar |

---

## Observações

- **Persistência real:** os dados sobrevivem a reinícios (MySQL).
- **Integrações simuladas:** SEFAZ, ANS e Fornecedor não fazem chamadas HTTP reais.
- **Componentes reutilizáveis:** lógica de negócio isolada em jars — base para a v2.
- **Senha do banco:** sempre via `DB_USER`/`DB_PASS`; nunca commitada.
- **Porta padrão:** `8080`.
