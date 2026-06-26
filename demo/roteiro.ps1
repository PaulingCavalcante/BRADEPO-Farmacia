# ============================================================================
# Roteiro de demonstração — BRADEPO-Farmacia (Projeto 2)
#
# Executa, em ordem, o fluxo que cobre todas as features para o vídeo.
# Pré-requisito: a aplicação rodando em http://localhost:8080
#   (em outro terminal:  cd farmacia-service ; mvn spring-boot:run)
#
# Rodar:  pwsh demo/roteiro.ps1     (ou clicar com botão direito > Run with PowerShell)
# ============================================================================

$ErrorActionPreference = "Continue"
$base = "http://localhost:8080"

function Passo($titulo) {
    Write-Host ""
    Write-Host "==================================================================" -ForegroundColor Cyan
    Write-Host "  $titulo" -ForegroundColor Cyan
    Write-Host "==================================================================" -ForegroundColor Cyan
}

function Get-Json($url) {
    try { Invoke-RestMethod -Uri $url -Method Get | ConvertTo-Json -Depth 6 }
    catch { Write-Host $_.Exception.Message -ForegroundColor Yellow }
}

function Post-Json($url, $body) {
    try {
        Invoke-RestMethod -Uri $url -Method Post -ContentType "application/json" `
            -Body ($body | ConvertTo-Json) | ConvertTo-Json -Depth 6
    } catch {
        # mostra o corpo de erro (ex.: 400/404/409) sem abortar o roteiro
        Write-Host $_.Exception.Message -ForegroundColor Yellow
    }
}

Passo "1) Catálogo: produtos cadastrados (seed do data.sql)"
Get-Json "$base/produtos"

Passo "2) Clientes cadastrados (seed)"
Get-Json "$base/clientes"

Passo "3) Cadastrar um produto novo (CRUD POST /produtos)"
Post-Json "$base/produtos" @{ nome = "Loratadina"; categoria = "MEDICAMENTO"; preco = 19.90; estoque = 30; controlado = $false }

Passo "4) Venda comum INTERNET — Maria (idosa + convenio) -> desconto de convenio 15%"
Post-Json "$base/venda" @{ cpf = "529.982.247-25"; produto = "Dipirona" }

Passo "5) Venda CONTROLADA com cliente — Rivotril (gera protocolo ANS) + desconto fabricante"
Post-Json "$base/venda" @{ cpf = "111.444.777-35"; produto = "Rivotril" }

Passo "6) Venda CONTROLADA sem CPF -> NEGADA (regra Fase 3)"
Post-Json "$base/venda" @{ produto = "Rivotril" }

Passo "7) Venda BALCAO com vendedor -> gera comissao (Fase 5)"
Post-Json "$base/venda" @{ produto = "Sabonete"; canal = "BALCAO"; vendedor = "Carlos" }

Passo "8) Venda avulsa (produto comum, sem CPF) -> AUTORIZADA, sem desconto"
Post-Json "$base/venda" @{ produto = "Pasta de Dente" }

Passo "9) CPF invalido -> NEGADA"
Post-Json "$base/venda" @{ cpf = "00000000000"; produto = "Dipirona" }

Passo "10) Produto nao cadastrado -> NEGADA"
Post-Json "$base/venda" @{ cpf = "529.982.247-25"; produto = "ProdutoInexistente" }

Passo "11) Notas/vendas emitidas (persistidas no MySQL)"
Get-Json "$base/notas"

Passo "12) Relatorio: vendas por periodo (junho/2026)"
Get-Json "$base/relatorios/vendas?inicio=2026-06-01&fim=2026-06-30"

Passo "13) Relatorio: produtos mais vendidos"
Get-Json "$base/relatorios/mais-vendidos"

Write-Host ""
Write-Host "Fim do roteiro." -ForegroundColor Green
