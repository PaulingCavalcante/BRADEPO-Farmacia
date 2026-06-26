# Banco de dados — `db/`

Entregável do Projeto 2: o banco **MySQL populado** vai junto do projeto, para o
professor ver dados reais sem precisar configurar nada.

## Arquivo

- **`farmacia-dump.sql`** — dump completo: cria o schema `farmacia`, as três
  tabelas (`produto`, `cliente`, `venda`) e já insere produtos, clientes e
  vendas de exemplo (comuns/controladas, internet/balcão, com/sem desconto).

## Dois caminhos para ter o banco com dados

**(a) Importar o dump pronto** (mais rápido para avaliar):

```bash
mysql -u root -p < db/farmacia-dump.sql
```

Depois confira:

```sql
USE farmacia;
SELECT * FROM produto;
SELECT * FROM venda;
```

**(b) Subir do zero pela aplicação** (o `data.sql` semeia produtos e clientes):

```bash
mvn clean install
cd farmacia-service
mvn spring-boot:run
```

No primeiro boot o Hibernate cria as tabelas (`ddl-auto=update`) e o
`src/main/resources/data.sql` popula produtos e clientes
(`spring.sql.init.mode=always`). As vendas você gera fazendo `POST /venda`
(ver `demo/roteiro.ps1` ou o `README.md` da raiz).

## Regerar este dump a partir do seu MySQL populado

Depois de subir a app e fazer algumas vendas:

```bash
mysqldump -u root -p --databases farmacia > db/farmacia-dump.sql
```

> A senha do MySQL **nunca** é commitada. A aplicação lê usuário/senha das
> variáveis de ambiente `DB_USER`/`DB_PASS` (padrão de dev: `root`/`root`).
