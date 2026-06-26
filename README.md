# BRADEPO Farmácia — Projeto 2

Sistema de vendas de uma rede de farmácias, refatorado para **arquitetura baseada
em componentes** (Maven multi-módulo). Cobre o fluxo completo de venda — validação
de CPF, consulta ao fornecedor, emissão de NF-e (SEFAZ), notificação de receitas
controladas (ANS), cálculo de desconto e comissão — com **persistência real em
MySQL** via Spring Data JPA.

As integrações externas (SEFAZ, ANS, Fornecedor) são **simuladas** dentro de
componentes-jar reutilizáveis; o que é real é a orquestração e o banco de dados.

## Arquitetura (módulos Maven)

| Módulo | Tipo | Papel |
|---|---|---|
| `bradeco-farmacia-parent` | pom | Agrega os módulos |
| `cpf-validator-component` | jar | Validação de CPF (módulo 11) |
| `sefaz-component` | jar | Emissão de NF-e (record `NotaFiscal`) |
| `ans-component` | jar | Notificação de medicamento controlado |
| `fornecedor-component` | jar | Disponibilidade no fornecedor (legado) |
| `desconto-component` | jar | Cálculo de desconto (progressivo/convênio) |
| `farmacia-service` | Spring Boot | Orquestra os componentes + REST + JPA/MySQL |

> Os componentes são jars puros (sem Spring Boot) — a lógica reutilizável mora
> neles, de forma que possam ser reaproveitados na v2 (mensageria). A persistência
> vive **só** no `farmacia-service`.

## Pré-requisitos

- **Java 17** e **Maven 3.8+** (`java --version`, `mvn --version`)
- **MySQL 8** rodando em `localhost:3306`

## Configuração do banco

A aplicação cria o schema (`createDatabaseIfNotExist=true`) e as tabelas
(`ddl-auto=update`) sozinha. As credenciais vêm de variáveis de ambiente
(padrão de dev `root`/`root`) — **a senha nunca é commitada**:

```powershell
$env:DB_USER = "root"            # ou um usuário dedicado
$env:DB_PASS = "SUA_SENHA_MYSQL"
```

> Sem privilégio para criar o schema? Crie antes:
> `CREATE DATABASE farmacia;` e um usuário com `GRANT ALL ON farmacia.*`.

## Como executar

```powershell
# 1) Compilar tudo (instala os jars dos componentes no ~/.m2)
mvn clean install

# 2) Subir a aplicação (lê DB_USER/DB_PASS do ambiente)
cd farmacia-service
mvn spring-boot:run
# aguarde: Started FarmaciaApplication ...  (porta 8080)
```

No primeiro boot, o `data.sql` popula **produtos** e **clientes** de exemplo.

### Banco já populado (entregável)

Há um dump pronto em [`db/farmacia-dump.sql`](db/farmacia-dump.sql) com produtos,
clientes e vendas de exemplo:

```bash
mysql -u root -p < db/farmacia-dump.sql
```

Detalhes em [`db/README.md`](db/README.md).

## Endpoints

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/venda` | Processa uma venda |
| `GET` | `/notas` | Lista as vendas autorizadas |
| `POST/GET/PUT/DELETE` | `/produtos` `/produtos/{id}` | CRUD de produtos |
| `POST/GET/PUT/DELETE` | `/clientes` `/clientes/{id}` | CRUD de clientes |
| `GET` | `/relatorios/vendas?inicio=&fim=` | Vendas por período (datas ISO) |
| `GET` | `/relatorios/mais-vendidos` | Ranking de produtos por quantidade |

### Corpo da venda (`POST /venda`)

```json
{ "cpf": "529.982.247-25", "produto": "Dipirona", "canal": "BALCAO", "vendedor": "Carlos" }
```

- `cpf` — opcional, **obrigatório** se o produto for controlado.
- `produto` — nome de um produto **cadastrado** (case-insensitive).
- `canal` — `INTERNET` (padrão) ou `BALCAO`. `BALCAO` exige `vendedor` e gera comissão.

### Regras de negócio

- **Produto controlado** (flag `controlado`): exige CPF válido; aciona a ANS.
- **Desconto**: cliente cadastrado tem desconto progressivo por faixa de valor;
  idoso **com** convênio recebe a maior vantagem entre convênio (15%) e fabricante.
- **Comissão**: vendas no balcão geram 5% sobre o valor líquido para o vendedor.
- **Estoque**: cada venda autorizada baixa uma unidade do produto.

## Roteiro de demonstração

Com a app no ar, rode o script que exercita todo o fluxo:

```powershell
pwsh demo/roteiro.ps1
```

### Exemplos rápidos (curl)

```bash
# Venda comum (cliente cadastrado idosa+convenio -> desconto)
curl -X POST http://localhost:8080/venda -H "Content-Type: application/json" \
  -d "{\"cpf\":\"529.982.247-25\",\"produto\":\"Dipirona\"}"

# Controlado sem CPF -> NEGADA
curl -X POST http://localhost:8080/venda -H "Content-Type: application/json" \
  -d "{\"produto\":\"Rivotril\"}"

# Venda no balcão (comissão)
curl -X POST http://localhost:8080/venda -H "Content-Type: application/json" \
  -d "{\"produto\":\"Sabonete\",\"canal\":\"BALCAO\",\"vendedor\":\"Carlos\"}"

# Relatórios
curl "http://localhost:8080/relatorios/vendas?inicio=2026-06-01&fim=2026-06-30"
curl http://localhost:8080/relatorios/mais-vendidos
```

## Fora de escopo (v2 planejada)

Mensageria (Kafka/RabbitMQ) e integração real com iFood. Os componentes-jar já
são a base reutilizável; a v2 troca apenas a camada de orquestração.
