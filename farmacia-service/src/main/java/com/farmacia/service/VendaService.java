package com.farmacia.service;

import com.farmacia.componentes.ans.AnsClient;
import com.farmacia.componentes.cpf.CpfValidator;
import com.farmacia.componentes.desconto.CalculadoraDesconto;
import com.farmacia.componentes.desconto.DescontoContexto;
import com.farmacia.componentes.desconto.DescontoResultado;
import com.farmacia.componentes.fornecedor.FornecedorAdapter;
import com.farmacia.componentes.sefaz.NotaFiscal;
import com.farmacia.componentes.sefaz.SefazClient;
import com.farmacia.dto.ResumoFinanceiro;
import com.farmacia.dto.VendaRequest;
import com.farmacia.dto.VendaResponse;
import com.farmacia.model.Canal;
import com.farmacia.model.Cliente;
import com.farmacia.model.Produto;
import com.farmacia.model.Venda;
import com.farmacia.repository.ClienteRepository;
import com.farmacia.repository.ProdutoRepository;
import com.farmacia.repository.VendaRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class VendaService {

    /** Comissão do vendedor em vendas de balcão: 5% sobre o valor líquido. */
    private static final BigDecimal COMISSAO_PERCENTUAL = new BigDecimal("0.05");

    private final CpfValidator cpfValidator;
    private final FornecedorAdapter fornecedor;
    private final SefazClient sefaz;
    private final AnsClient ans;
    private final CalculadoraDesconto calculadoraDesconto;
    private final VendaRepository repository;
    private final ProdutoRepository produtoRepository;
    private final ClienteRepository clienteRepository;

    public VendaService(CpfValidator cpfValidator,
                        FornecedorAdapter fornecedor,
                        SefazClient sefaz,
                        AnsClient ans,
                        CalculadoraDesconto calculadoraDesconto,
                        VendaRepository repository,
                        ProdutoRepository produtoRepository,
                        ClienteRepository clienteRepository) {
        this.cpfValidator = cpfValidator;
        this.fornecedor = fornecedor;
        this.sefaz = sefaz;
        this.ans = ans;
        this.calculadoraDesconto = calculadoraDesconto;
        this.repository = repository;
        this.produtoRepository = produtoRepository;
        this.clienteRepository = clienteRepository;
    }

    public VendaResponse processar(VendaRequest req) {
        String cpf = req.cpf();
        String nomeProduto = req.produto();

        if (nomeProduto == null || nomeProduto.trim().isEmpty()) {
            return negada("Requisição vazia");
        }

        // Fase 5: canal da venda. INTERNET é o padrão quando omitido; BALCAO exige vendedor.
        Canal canal;
        if (req.canal() == null || req.canal().isBlank()) {
            canal = Canal.INTERNET;
        } else {
            try {
                canal = Canal.valueOf(req.canal().trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                return negada("canal invalido: use INTERNET ou BALCAO");
            }
        }
        String vendedor = (req.vendedor() == null || req.vendedor().isBlank())
                ? null : req.vendedor().trim();
        if (canal == Canal.BALCAO && vendedor == null) {
            return negada("venda no balcao exige o vendedor");
        }
        if (canal != Canal.BALCAO) {
            vendedor = null; // comissão/vendedor só fazem sentido no balcão
        }

        // O produto precisa estar cadastrado (fonte da verdade sobre controlado/estoque/preço).
        Optional<Produto> cadastrado = produtoRepository.findByNomeIgnoreCase(nomeProduto.trim());
        if (cadastrado.isEmpty()) {
            return negada("produto nao cadastrado");
        }
        Produto produto = cadastrado.get();

        // Regra Fase 3: produto controlado exige CPF do cliente (válido); produto
        // comum pode ser vendido sem CPF (NF avulsa), mas se o CPF vier, valida.
        boolean temCpf = cpf != null && !cpf.isBlank();
        if (produto.isControlado()) {
            if (!temCpf) {
                return negada("produto controlado exige CPF do cliente");
            }
            if (!cpfValidator.validar(cpf)) {
                return negada("CPF invalido");
            }
        } else if (temCpf && !cpfValidator.validar(cpf)) {
            return negada("CPF invalido");
        }

        // Disponibilidade no fornecedor (componente externo) e estoque local da farmácia.
        if (!fornecedor.consultar(produto.getNome())) {
            return negada("produto indisponivel no fornecedor");
        }
        if (produto.getEstoque() <= 0) {
            return negada("produto sem estoque");
        }

        NotaFiscal nota = new NotaFiscal(UUID.randomUUID().toString(), cpf, produto.getNome());
        String protocoloSefaz = sefaz.enviarNota(nota);

        String protocoloAns = null;
        if (produto.isControlado()) {
            protocoloAns = ans.enviarReceita(cpf, produto.getNome());
        }

        // Fase 4: desconto. O cliente cadastrado (encontrado por CPF) habilita o
        // desconto progressivo; idoso + convênio podem render a maior vantagem.
        Optional<Cliente> cliente = temCpf ? clienteRepository.findByCpf(cpf.trim()) : Optional.empty();
        DescontoResultado desconto = calculadoraDesconto.calcular(new DescontoContexto(
                produto.getPreco(),
                cliente.isPresent(),
                cliente.map(Cliente::isIdoso).orElse(false),
                cliente.map(Cliente::isConvenio).orElse(false)));

        // Fase 5: comissão do vendedor só em venda de balcão (5% do valor líquido).
        BigDecimal comissao = (canal == Canal.BALCAO)
                ? desconto.valorLiquido().multiply(COMISSAO_PERCENTUAL).setScale(2, RoundingMode.HALF_UP)
                : null;

        // Baixa de estoque do produto cadastrado.
        produto.setEstoque(produto.getEstoque() - 1);
        produtoRepository.save(produto);

        // Persiste a venda autorizada no banco (NEGADAs não são gravadas, como antes).
        Venda venda = new Venda("AUTORIZADA", cpf, produto.getNome(), nota.id(),
                protocoloSefaz, protocoloAns, null, LocalDateTime.now());
        venda.setValorBruto(produto.getPreco());
        venda.setPercentualDesconto(desconto.percentual());
        venda.setValorDesconto(desconto.valorDesconto());
        venda.setValorLiquido(desconto.valorLiquido());
        venda.setDescricaoDesconto(desconto.descricao());
        venda.setCanal(canal);
        venda.setVendedor(vendedor);
        venda.setComissao(comissao);
        repository.save(venda);

        return new VendaResponse("AUTORIZADA", nota, protocoloSefaz, protocoloAns, null,
                canal.name(), vendedor,
                new ResumoFinanceiro(produto.getPreco(), desconto.percentual(),
                        desconto.valorDesconto(), desconto.valorLiquido(), desconto.descricao(),
                        comissao));
    }

    public List<VendaResponse> listarNotas() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    /** Resposta de venda recusada (sem nota nem valores). */
    private VendaResponse negada(String motivo) {
        return new VendaResponse("NEGADA", null, null, null, motivo, null, null, null);
    }

    /** Reconstrói o DTO de resposta a partir da entidade persistida. */
    private VendaResponse toResponse(Venda v) {
        NotaFiscal nota = new NotaFiscal(v.getNotaId(), v.getCpf(), v.getProduto());
        ResumoFinanceiro financeiro = new ResumoFinanceiro(v.getValorBruto(),
                v.getPercentualDesconto(), v.getValorDesconto(), v.getValorLiquido(),
                v.getDescricaoDesconto(), v.getComissao());
        String canal = v.getCanal() == null ? null : v.getCanal().name();
        return new VendaResponse(v.getStatus(), nota, v.getProtocoloSefaz(),
                v.getProtocoloAns(), v.getMotivo(), canal, v.getVendedor(), financeiro);
    }
}
