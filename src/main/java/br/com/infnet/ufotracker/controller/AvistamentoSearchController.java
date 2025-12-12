package br.com.infnet.ufotracker.controller;


import br.com.infnet.ufotracker.dtos.*;
import br.com.infnet.ufotracker.service.AvistamentoSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/avistamentos/search")
@RequiredArgsConstructor
public class AvistamentoSearchController {
    private final AvistamentoSearchService searchService;

    /**
     * Busca simples por texto na descrição.
     *
     * GET /api/avistamentos/search/texto?texto=...&page=0&size=10
     */
    @GetMapping("/texto")
    public ResponseEntity<List<AvistamentoDoc>> buscarPorTexto(
            @RequestParam String texto,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) throws IOException {

        var resultados = searchService.buscarPorDescricao(texto, page, size);
        return ResponseEntity.ok(resultados);
    }

    /**
     * Busca avançada:
     * - estado (obrigatório)
     * - tipoObjeto (opcional)
     * - confiabilidadeMin (opcional)
     *
     * GET /api/avistamentos/search/avancada?estado=RJ&tipoObjeto=cilindro&confiabilidadeMin=70&page=0&size=20
     */
    @GetMapping("/avancada")
    public ResponseEntity<List<AvistamentoDoc>> buscaAvancada(
            @RequestParam String estado,
            @RequestParam(required = false) String tipoObjeto,
            @RequestParam(required = false) Integer confiabilidadeMin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) throws IOException {

        var resultados = searchService.buscaAvancada(estado, tipoObjeto, confiabilidadeMin, page, size);
        return ResponseEntity.ok(resultados);
    }
    @GetMapping("/ultimo-ano")
    public ResponseEntity<?> getUltimoAno(

    ) throws IOException {

        List<TipoObjetoEstatistica> tipoObjetoEstatisticas = searchService.agruparPorTipoObjetoUltimoAno();
        return ResponseEntity.ok(tipoObjetoEstatisticas);
    }


    @GetMapping("/media-confiabilidade")
    public ResponseEntity<?> getMediaConfiabilidade(

    ) throws IOException {

        double media = searchService.calcularMediaConfiabilidade();
        return ResponseEntity.ok(Map.of("media",media));
    }
    @GetMapping("/estatisticas-confiabilidade")
    public ResponseEntity<?> getEstatisticas(

    ) throws IOException {

        EstatisticasConfiabilidade estatisticasConfiabilidade = searchService.calcularEstatisticasConfiabilidade();
        return ResponseEntity.ok(estatisticasConfiabilidade);
    }

    /**
     * Agregação: quantos avistamentos por estado.
     *
     * GET /api/avistamentos/search/agg/por-estado
     */
    @GetMapping("/agg/por-estado")
    public ResponseEntity<Map<String, Long>> contagemPorEstado() throws IOException {
        var mapa = searchService.contagemPorEstado();
        return ResponseEntity.ok(mapa);
    }

    /**
     * Busca por proximidade geográfica (geo_distance).
     *
     * GET /api/avistamentos/search/perto?lat=-22.9&lon=-43.1&distanciaKm=100&size=20
     */
    @GetMapping("/perto")
    public ResponseEntity<List<AvistamentoDoc>> buscarPerto(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "50") String distanciaKm,
            @RequestParam(defaultValue = "10") int size
    ) throws IOException {
        var resultados = searchService.buscarPerto(lat, lon, distanciaKm, size);
        return ResponseEntity.ok(resultados);
    }
    // ---------------------------------------------
    // 5) NOVO ENDPOINT: Top Avistamentos na semana anterior
    // ---------------------------------------------
    @GetMapping("/top-semana")
    public ResponseEntity<List<TopAvistamento>> topSemanaAnterior(
            @RequestParam String dataBase
    ) throws IOException {

        var resultados = searchService.topSemanaAnterior(dataBase);
        return ResponseEntity.ok(resultados);
    }
    @GetMapping("/ranking-semana")
    public ResponseEntity<List<EstadoConfiabilidadeDto>> rankingSemanaAnterior(
            @RequestParam String dataBase
    ) throws IOException {

        String inicioSemana = searchService.getInicioSemana(dataBase);
        var resultados = searchService.getRankingSemanaAnterior(inicioSemana);
        return ResponseEntity.ok(resultados);
    }
    @GetMapping("/por-horario")
    public ResponseEntity<?> getPorHorario() throws IOException {
        return ResponseEntity.ok(searchService.timelinePorHoraUltimoAno());
    }

}
