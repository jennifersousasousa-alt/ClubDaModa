package org.example.service;

import org.example.exception.RecursoNaoEncontradoException;
import org.example.exception.RegraNegocioException;
import org.example.model.Cliente;
import org.example.model.Endereco;
import org.example.repository.EnderecoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EnderecoService {

    private final EnderecoRepository repository;
    private final ClienteService clienteService;

    public EnderecoService(EnderecoRepository repository, ClienteService clienteService) {
        this.repository = repository;
        this.clienteService = clienteService;
    }

    public List<Endereco> listar() {
        return repository.findAll();
    }

    public Endereco buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Endereço com id " + id + " não encontrado"));
    }

    @Transactional
    public Endereco criar(Endereco endereco) {
        endereco.setId(null);

        if (endereco.getCliente() == null || endereco.getCliente().getId() == null) {
            throw new RegraNegocioException("Informe o id do cliente dono do endereço");
        }

        Cliente cliente = clienteService.buscarPorId(endereco.getCliente().getId());

        if (repository.existsByClienteId(cliente.getId())) {
            throw new RegraNegocioException("O cliente já possui um endereço cadastrado");
        }

        endereco.setCliente(cliente);
        return repository.save(endereco);
    }

    // Na atualização só os dados do endereço mudam; o cliente dono continua o mesmo.
    @Transactional
    public Endereco atualizar(Long id, Endereco dados) {
        Endereco existente = buscarPorId(id);
        existente.setRua(dados.getRua());
        existente.setNumero(dados.getNumero());
        existente.setCidade(dados.getCidade());
        existente.setEstado(dados.getEstado());
        existente.setCep(dados.getCep());
        return repository.save(existente);
    }

    @Transactional
    public void excluir(Long id) {
        Endereco endereco = buscarPorId(id);
        repository.delete(endereco);
    }
}