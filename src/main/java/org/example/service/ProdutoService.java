package org.example.service;

import org.example.exception.RecursoNaoEncontradoException;
import org.example.exception.RegraNegocioException;
import org.example.model.Categoria;
import org.example.model.Produto;
import org.example.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProdutoService {

    private final ProdutoRepository repository;
    private final CategoriaService categoriaService;

    public ProdutoService(ProdutoRepository repository, CategoriaService categoriaService) {
        this.repository = repository;
        this.categoriaService = categoriaService;
    }

    public List<Produto> listar() {
        return repository.findAll();
    }

    public Produto buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Produto com id " + id + " não encontrado"));
    }

    @Transactional
    public Produto criar(Produto produto) {
        produto.setId(null);
        produto.setCategoria(resolverCategoria(produto.getCategoria()));
        return repository.save(produto);
    }

    @Transactional
    public Produto atualizar(Long id, Produto dados) {
        Produto existente = buscarPorId(id);
        existente.setNome(dados.getNome());
        existente.setDescricao(dados.getDescricao());
        existente.setPreco(dados.getPreco());
        existente.setEstoque(dados.getEstoque());
        existente.setCategoria(resolverCategoria(dados.getCategoria()));
        return repository.save(existente);
    }

    @Transactional
    public void excluir(Long id) {
        Produto produto = buscarPorId(id);
        repository.delete(produto);
    }

    private Categoria resolverCategoria(Categoria informada) {
        if (informada == null || informada.getId() == null) {
            throw new RegraNegocioException("Informe o id da categoria do produto");
        }
        return categoriaService.buscarPorId(informada.getId());
    }
}
