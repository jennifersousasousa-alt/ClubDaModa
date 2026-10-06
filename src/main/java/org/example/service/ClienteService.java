package org.example.service;

import org.example.exception.RecursoNaoEncontradoException;
import org.example.exception.RegraNegocioException;
import org.example.model.Cliente;
import org.example.repository.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository repository;

    public ClienteService(ClienteRepository repository) {
        this.repository = repository;
    }

    public List<Cliente> listar() {
        return repository.findAll();
    }

    public Cliente buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Cliente com id " + id + " não encontrado"));
    }

    @Transactional
    public Cliente criar(Cliente cliente) {
        cliente.setId(null);
        validarEmailDisponivel(cliente.getEmail(), null);
        return repository.save(cliente);
    }

    @Transactional
    public Cliente atualizar(Long id, Cliente dados) {
        Cliente existente = buscarPorId(id);
        validarEmailDisponivel(dados.getEmail(), id);
        existente.setNome(dados.getNome());
        existente.setEmail(dados.getEmail());
        existente.setTelefone(dados.getTelefone());
        return repository.save(existente);
    }

    @Transactional
    public void excluir(Long id) {
        Cliente cliente = buscarPorId(id);
        repository.delete(cliente);
    }

    // Garante que nenhum outro cliente use o mesmo e-mail.
    private void validarEmailDisponivel(String email, Long idAtual) {
        repository.findByEmail(email).ifPresent(outro -> {
            if (!outro.getId().equals(idAtual)) {
                throw new RegraNegocioException("Já existe um cliente com o e-mail: " + email);
            }
        });
    }
}