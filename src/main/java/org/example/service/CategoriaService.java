package org.example.service;

import org.example.exception.RecursoNaoEncontradoException;
import org.example.model.Categoria;
import org.example.repository.CategoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository repository;

    public CategoriaService(CategoriaRepository repository) {
        this.repository = repository;
    }

    public List<Categoria> listar() {
        return repository.findAll();
    }

    public Categoria buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Categoria com id " + id + " não encontrada"));
    }

    @Transactional
    public Categoria criar(Categoria categoria) {
        categoria.setId(null);
        return repository.save(categoria);
    }

    @Transactional
    public Categoria atualizar(Long id, Categoria dados) {
        Categoria existente = buscarPorId(id);
        existente.setNome(dados.getNome());
        existente.setDescricao(dados.getDescricao());
        return repository.save(existente);
    }

    @Transactional
    public void excluir(Long id) {
        Categoria categoria = buscarPorId(id);
        repository.delete(categoria);
    }
}
