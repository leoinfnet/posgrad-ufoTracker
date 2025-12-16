package br.com.infnet.ufotracker.controller;

import br.com.infnet.ufotracker.dtos.AvistamentoDoc;
import br.com.infnet.ufotracker.service.AvistamentoSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class IndexController {
    private final AvistamentoSearchService searchService;
    /**
     * Busca simples por texto na descrição.
     *
     * GET /api/avistamentos/search/texto?texto=...&page=0&size=10
     */
    @GetMapping("/")
    public ResponseEntity<List<AvistamentoDoc>> buscarPorTexto(
            @RequestParam String texto,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) throws IOException {

        var resultados = searchService.buscarPorDescricao(texto, page, size);
        return ResponseEntity.ok(resultados);
    }
}
