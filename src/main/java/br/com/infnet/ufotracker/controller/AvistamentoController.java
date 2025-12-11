package br.com.infnet.ufotracker.controller;

import br.com.infnet.ufotracker.model.Avistamento;
import br.com.infnet.ufotracker.service.AvistamentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/avistamentos")
@RequiredArgsConstructor
public class AvistamentoController {
    private final AvistamentoService service;

    @GetMapping
    public Page<Avistamento> findAll(@RequestParam(required = false, defaultValue = "0") Integer page ,
                                     @RequestParam(required = false, defaultValue = "10") Integer size){

        return service.listar(page, size);
    }
    //avistamentos/1
    @GetMapping("/{id}")
    public ResponseEntity<Avistamento> obter(@PathVariable UUID id){
        Optional<Avistamento> avistamento = service.buscarPeloId(id);
        //Method Reference
        return avistamento.map(ResponseEntity::ok)
                .orElseGet( ResponseEntity.notFound()::build);
    }
    // [GET] .../avistamento?page=1&size=10
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Avistamento salvar(@RequestBody Avistamento avistamento){
        return service.salvar(avistamento);
    }
    @PutMapping("/{id}")
    public ResponseEntity<Avistamento> atualizar(@PathVariable UUID id, @RequestBody Avistamento avistamento){
        try{
            Avistamento atualizado = service.atualizar(id, avistamento);
            return ResponseEntity.ok(atualizado);
        }catch (NoSuchElementException e){
            return ResponseEntity.notFound().build();
        }

    }


}


//Jackson   -> Classe -> Json < -- Classe
//[GET/POST/DELETE/PUT/OPTIONS] http://localhost:8080/avistamento
/*
GET  /avistamento -> GETALL
GET /avistamento/id -> FINDBYID
DELETE /avistamento/id - DELETE BY ID
PUT /avistamento/id -> UPDATE
OPTIONS -> Recupera os cabeçalhos da resposta
 */
//avistamentos?page=10&size=10
//WEB  - MOBILE