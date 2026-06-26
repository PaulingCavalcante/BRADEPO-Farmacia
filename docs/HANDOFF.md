# HANDOFF — Refatoração BRADEPO-Farmacia (Projeto 2)

> Status final em **2026-06-26**. Leia também `CLAUDE.md`, `PLANO.md`, `README.md`.

## TL;DR — TODAS as fases concluídas

Todas as 7 fases do `PLANO.md` estão **implementadas, compilando e commitadas** na
branch `Farmacia2`. `mvn clean install` → BUILD SUCCESS (6 módulos). O fluxo foi
validado por um smoke test com H2 em memória (contexto Spring sobe, mapeamentos
JPA OK, venda/desconto/comissão/ANS e relatórios funcionam).

| Fase | O quê | Commit |
|---|---|---|
| 1 | Persistência MySQL/JPA (entidade `Venda`, `JpaRepository`) | `8f49791` |
| 2 | Cadastro de `Produto` (CRUD) + `data.sql` (seed) + baixa de estoque | `0f736df` |
| 3 | `Cliente` (CRUD) + CPF obrigatório só p/ produto controlado | `e3820ad` |
| 4 | Novo módulo-jar `desconto-component` (progressivo/convênio) | `42874ad` |
| 5 | Canal `INTERNET`/`BALCAO` + comissão do vendedor | `2273a53` |
| 6 | Relatórios: vendas por período + mais vendidos | `bb591eb` |
| 7 | Docs (README/DOCUMENTACAO) + `db/farmacia-dump.sql` + `demo/roteiro.ps1` | `ea847ec` |

## ✅ Entregável já completo

- Código das 6 partes (5 componentes-jar + `farmacia-service`).
- Banco **populado** entregue: `db/farmacia-dump.sql` (produtos, clientes e vendas
  de exemplo) — importável com `mysql -u root -p < db/farmacia-dump.sql`.
- Seed automático no boot: `farmacia-service/src/main/resources/data.sql`.
- Roteiro do vídeo: `demo/roteiro.ps1`.

## ⏭️ Único passo opcional restante (precisa da senha do MySQL)

Validar end-to-end no **MySQL real** e, se quiser, regerar um dump "de verdade":

```powershell
# 1) build
mvn clean install

# 2) subir com a senha real (NÃO committar a senha)
$env:DB_USER="root"            # ou usuário dedicado
$env:DB_PASS="SUA_SENHA_REAL"
cd farmacia-service ; mvn spring-boot:run
# aguarde: Started FarmaciaApplication ... (8080)

# 3) noutro terminal: exercitar todo o fluxo
pwsh ..\demo\roteiro.ps1

# 4) (opcional) regerar o dump real a partir do banco populado
mysqldump -u root -p --databases farmacia > db\farmacia-dump.sql
```

Teste de aceite da persistência: faça uma venda, **reinicie a app** e confirme que
`GET /notas` ainda mostra a venda → persistiu no MySQL. ✅

## Ambiente (confirmado)

- Maven 3.9.x sob JDK 20 (compila target Java 17). Spring Boot 3.2.5 / Hibernate 6.4.
- MySQL 8 (serviço MySQL80, porta 3306). Senha do `root` **não** é `root` —
  use `DB_PASS` por env var, ou crie usuário dedicado `farmacia`/`farmacia123`.
- Windows 11, PowerShell. Projeto em OneDrive.
