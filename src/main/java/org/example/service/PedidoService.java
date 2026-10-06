package org.example.service;

import org.example.exception.RecursoNaoEncontradoException;
import org.example.exception.RegraNegocioException;
import org.example.model.Cliente;
import org.example.model.Pedido;
import org.example.model.Produto;
import org.example.model.StatusPedido;
import org.example.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class PedidoService {

    private final PedidoRepository repository;
    private final ClienteService clienteService;
    private final ProdutoService produtoService;

    public PedidoService(PedidoRepository repository,
                         ClienteService clienteService,
                         ProdutoService produtoService) {
        this.repository = repository;
        this.clienteService = clienteService;
        this.produtoService = produtoService;
    }

    public List<Pedido> listar() {
        return repository.findAll();
    }

    public Pedido buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Pedido com id " + id + " não encontrado"));
    }

    @Transactional
    public Pedido criar(Pedido pedido) {
        pedido.setId(null);
        pedido.setDataPedido(LocalDateTime.now());
        pedido.setStatus(StatusPedido.PENDENTE);
        pedido.setCliente(resolverCliente(pedido.getCliente()));
        pedido.setProdutos(resolverProdutos(pedido.getProdutos()));
        pedido.setValorTotal(calcularTotal(pedido.getProdutos()));
        return repository.save(pedido);
    }

    @Transactional
    public Pedido atualizar(Long id, Pedido dados) {
        Pedido existente = buscarPorId(id);

        existente.setCliente(resolverCliente(dados.getCliente()));
        existente.setProdutos(resolverProdutos(dados.getProdutos()));
        existente.setValorTotal(calcularTotal(existente.getProdutos()));

        if (dados.getStatus() != null) {
            existente.setStatus(dados.getStatus());
        }

        return repository.save(existente);
    }

    @Transactional
    public void excluir(Long id) {
        Pedido pedido = buscarPorId(id);
        repository.delete(pedido);
    }

    private Cliente resolverCliente(Cliente informado) {
        if (informado == null || informado.getId() == null) {
            throw new RegraNegocioException("Informe o id do cliente do pedido");
        }
        return clienteService.buscarPorId(informado.getId());
    }

    // A requisição traz só os ids dos produtos; aqui buscamos cada produto completo no banco.
    private List<Produto> resolverProdutos(List<Produto> informados) {
        List<Produto> encontrados = new ArrayList<>();
        Set<Long> idsJaUsados = new HashSet<>();

        for (Produto informado : informados) {
            if (informado.getId() == null) {
                throw new RegraNegocioException("Informe o id de cada produto do pedido");
            }
            if (!idsJaUsados.add(informado.getId())) {
                throw new RegraNegocioException(
                        "O produto " + informado.getId() + " foi informado mais de uma vez");
            }
            encontrados.add(produtoService.buscarPorId(informado.getId()));
        }

        return encontrados;
    }

    private BigDecimal calcularTotal(List<Produto> produtos) {
        BigDecimal total = BigDecimal.ZERO;
        for (Produto produto : produtos) {
            total = total.add(produto.getPreco());
        }
        return total;
    }
}