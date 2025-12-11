package br.com.infnet.ufotracker.service;

import br.com.infnet.ufotracker.model.Avistamento;
import br.com.infnet.ufotracker.repository.AvistamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AvistamentoService {

    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");
    //Injecao de Dependecia
    private final AvistamentoRepository repository;
    public Avistamento salvar(Avistamento avistamento) {
        if(avistamento.getDataHora() == null){
            avistamento.setDataHora(OffsetDateTime.now(ZONE));
        }
        return repository.save(avistamento);
    }

    public Page<Avistamento> listar(Integer page, Integer size){
        int p = page == null ? 0 : Math.max(0, page);
        int s = size == null ? 0 : Math.max(1, size);
        PageRequest pageRequest = PageRequest.of(p, s);
        return repository.findAll(pageRequest);

    }
    //Objeto do tipo esfera, observado realizando movimentos erráticos no céu, com testemunhas relatando sensação de estática no ar.
    // Documentos {} //Lucene -> Apache Indexador Textual
    // Chave Valor
    // Grafo
    // Colunares WIde Rows
    //Hispters
    public Optional<Avistamento> buscarPeloId(UUID id) {
        return repository.findById(id);
    }
    public Avistamento atualizar(UUID id, Avistamento novoAvistamento) {
        Optional<Avistamento> doBanco = buscarPeloId(id);
        Avistamento localizado = doBanco.orElseThrow(() ->
            new NoSuchElementException("Nenhum avistamento foi encontrado." + id));

        if(novoAvistamento.getDataHora() != null) localizado.setDataHora(novoAvistamento.getDataHora());
        if(novoAvistamento.getLatitude() != null) localizado.setLatitude(novoAvistamento.getLatitude());
        if(novoAvistamento.getLongitude() != null) localizado.setLongitude(novoAvistamento.getLongitude());
        if(novoAvistamento.getCidade() != null) localizado.setCidade(novoAvistamento.getCidade());
        if(novoAvistamento.getConfiabilidade() != null) localizado.setConfiabilidade(novoAvistamento.getConfiabilidade());
        return repository.save(localizado);
    }
}
