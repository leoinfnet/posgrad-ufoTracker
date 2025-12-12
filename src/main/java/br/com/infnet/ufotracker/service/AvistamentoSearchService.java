package br.com.infnet.ufotracker.service;

import br.com.infnet.ufotracker.dtos.*;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.LongTermsAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.LongTermsBucket;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AvistamentoSearchService {


    private static final String INDEX = "ufo-avistamentos";

    private final ElasticsearchClient esClient;


    public List<PontoTimeline> timelinePorHoraUltimoAno() throws IOException {
        return List.of(
                new PontoTimeline("00h", 240),
                new PontoTimeline("03h", 401),
                new PontoTimeline("06h", 31),
                new PontoTimeline("09h", 6),
                new PontoTimeline("12h", 5),
                new PontoTimeline("15h", 9),
                new PontoTimeline("18h", 71),
                new PontoTimeline("21h", 103)
        );

    }


    public List<TipoObjetoEstatistica> agruparPorTipoObjetoUltimoAno() throws IOException {
        SearchResponse<Void> response = esClient.search(s -> s
                        .index(INDEX)
                        .size(0)
                        .query(q -> q
                                .range(r -> r.date(f -> f
                                                .field("dataHora")
                                                .gte("now-1y/y")
                                                .lte("now")
                                        )
                                )
                        )
                        .aggregations("por_tipo_objeto", agg -> agg
                                .terms(t -> t
                                        .field("tipoObjeto") //
                                        .size(20)
                                )
                        ),
                Void.class
        );

        StringTermsAggregate termos =
                response.aggregations()
                        .get("por_tipo_objeto")
                        .sterms();

        List<TipoObjetoEstatistica> resultado = new ArrayList<>();

        for (StringTermsBucket bucket : termos.buckets().array()) {
            String tipo = bucket.key().stringValue();
            long qtde = bucket.docCount();

            resultado.add(new TipoObjetoEstatistica(tipo, qtde));
        }

        return resultado;
    }

    public List<AvistamentoDoc> buscarPorDescricao(String texto, int page, int size) throws IOException {
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, size);
        int from = safePage * safeSize;

        SearchResponse<AvistamentoDoc> response = esClient.search(s -> s
                        .index(INDEX)
                        .from(from)              // "from": ...
                        .size(safeSize)          // "size": ...
                        .trackScores(true)       // "track_scores": true
                        .query(q -> q
                                .match(m -> m
                                        .field("descricao")
                                        .query(texto)    // "query": texto vindo do parâmetro
                                        .boost(2.0f)     // "boost": 2.0
                                        .fuzziness("AUTO")
                                )
                        )
                        .highlight(h -> h
                                .preTags("<mark><strong><em>")
                                .postTags("</em></strong></mark>")
                                .fields("cidade", hf -> hf)
                                .fields("descricao", hf -> hf)
                        )
                        .sort(so -> so
                                .score(sc -> sc.order(SortOrder.Desc)) // "sort": [ { "_score": { "order": "desc" } } ]
                        ),
                AvistamentoDoc.class
        );

        List<AvistamentoDoc> docs = new ArrayList<>();

        for (Hit<AvistamentoDoc> hit : response.hits().hits()) {
            AvistamentoDoc doc = hit.source();
            if (doc == null) {
                continue;
            }

            doc.setScore(hit.score());

            Map<String, List<String>> highlight = hit.highlight();
            if (highlight != null) {
                List<String> descHl = highlight.get("descricao");
                if (descHl != null && !descHl.isEmpty()) {
                    doc.setDescricaoFormatada(descHl.get(0)); // descrição já com <em>...</em>
                }
            }

            docs.add(doc);
        }

        return docs;
    }



    // 1) Busca simples por texto na descrição
    public List<AvistamentoDoc> buscarPorDescricaoOld(String texto, int page, int size) throws IOException {
        int from = Math.max(0, page) * Math.max(1, size);
        ObjectMapper objectMapper = new ObjectMapper();
        SearchResponse<AvistamentoDoc> response = esClient.search(s -> s
                        .index(INDEX)
                        .from(from)
                        .size(size)
                        .query(q -> q
                                .match(m -> m
                                        .field("descricao")
                                        .query(texto)
                                )
                        )
                        .sort(so -> so
                                .field(f -> f
                                        .field("dataHora")
                                        .order(SortOrder.Desc)
                                )
                        ),
                AvistamentoDoc.class
        );

        return response.hits().hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .toList();
    }

    // 2) Filtro por estado + tipoObjeto + confiabilidade mínima
    public List<AvistamentoDoc> buscaAvancada(String estado, String tipoObjeto,
                                              Integer confiabilidadeMin,
                                              int page, int size) throws IOException {
        int from = Math.max(0, page) * Math.max(1, size);

        SearchResponse<AvistamentoDoc> response = esClient.search(s -> s
                        .index(INDEX)
                        .from(from)
                        .size(size)
                        .query(q -> q
                                .bool(b -> {

                                    // estado é obrigatório nesse cenário
                                    b.must(m -> m
                                            .term(t -> t
                                                    .field("estado")
                                                    .value(estado)
                                            )
                                    );

                                    // tipoObjeto é opcional
                                    if (tipoObjeto != null && !tipoObjeto.isBlank()) {
                                        b.must(m -> m
                                                .term(t -> t
                                                        .field("tipoObjeto")
                                                        .value(tipoObjeto)
                                                )
                                        );
                                    }

                                    // confiabilidade mínima opcional (range numérico)
                                    if (confiabilidadeMin != null) {
                                        b.must(m -> m
                                                .range(r -> r
                                                        .number(n -> n
                                                                .field("confiabilidade")
                                                                .gte(confiabilidadeMin.doubleValue())
                                                        )
                                                )
                                        );
                                    }

                                    return b;
                                })
                        ),
                AvistamentoDoc.class
        );

        return response.hits().hits().stream()
                .map(h -> h.source())
                .toList();
    }

    // 3) Agregação: quantos avistamentos por estado
    public Map<String, Long> contagemPorEstado() throws IOException {
        SearchResponse<Void> response = esClient.search(s -> s
                        .index(INDEX)
                        .size(0)
                        .aggregations("por_estado", a -> a
                                .terms(t -> t
                                        .field("estado")
                                )
                        ),
                Void.class
        );

        var agg = response.aggregations().get("por_estado").sterms();
        Map<String, Long> resultado = new LinkedHashMap<>();

        for (StringTermsBucket bucket : agg.buckets().array()) {
            resultado.put(bucket.key().stringValue(), bucket.docCount());
        }
        return resultado;
    }

    // 4) Geo: perto de uma coordenada
    public List<AvistamentoDoc> buscarPerto(double lat, double lon, String distanciaKm,
                                            int size) throws IOException {

        SearchResponse<AvistamentoDoc> response = esClient.search(s -> s
                        .index(INDEX)
                        .size(size)
                        .query(q -> q
                                .geoDistance(g -> g
                                        .field("location")
                                        .distance(distanciaKm + "km")
                                        .location(l -> l
                                                .latlon(ll -> ll
                                                        .lat(lat)
                                                        .lon(lon)
                                                )
                                        )
                                )
                        ),
                AvistamentoDoc.class
        );

        return response.hits().hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .toList();
    }
    @Cacheable(
            value = "resumoTopSemana",
            key = "'ufo:semana:' + #dataBaseIso"
    )
    public List<TopAvistamento> topSemanaAnterior(String dataBaseIso) throws IOException {
        LocalDate dataBase = LocalDate.parse(dataBaseIso);

        // Pega segunda-feira da semana da data recebida
        LocalDate segundaDaSemana = dataBase.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        // Agora vamos para a semana anterior
        LocalDate inicioSemanaAnterior = segundaDaSemana.minusWeeks(1);
        LocalDate inicioSemanaAtual = segundaDaSemana;

        // Converter para OffsetDateTime UTC (padrão bom para ES)
        OffsetDateTime inicio = inicioSemanaAnterior.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime fim = inicioSemanaAtual.atStartOfDay().atOffset(ZoneOffset.UTC);


        SearchResponse<Void> response = esClient.search(s -> s
                        .index(INDEX)
                        .size(0)
                        .query(q -> q
                                .range(r -> r
                                        .date(n -> n
                                                .field("dataHora")
                                                .gte(inicio.toString())
                                                .lt(fim.toString())
                                        )
                                )
                        )
                        .aggregations("por_estado", a -> a
                                .terms(t -> t
                                        .field("estado")
                                        .size(50)
                                )
                                .aggregations("top_por_confiabilidade", ta -> ta
                                        .topHits(th -> th
                                                .size(1)
                                                .sort(st -> st
                                                        .field(f -> f
                                                                .field("confiabilidade")
                                                                .order(SortOrder.Desc)
                                                        )
                                                )
                                        )
                                )
                        ),
                Void.class
        );

        var buckets = response.aggregations()
                .get("por_estado")
                .sterms()
                .buckets()
                .array();

        return buckets.stream()
                .map(bucket -> {
                    // hits do top_hits
                    var hitsAgg = bucket.aggregations()
                            .get("top_por_confiabilidade")
                            .topHits()
                            .hits()
                            .hits();

                    if (hitsAgg.isEmpty()) {
                        return null;
                    }

                    Hit<JsonData> hit = (Hit<JsonData>) hitsAgg.get(0);

                    AvistamentoDoc doc = hit.source() != null
                            ? hit.source().to(AvistamentoDoc.class)
                            : null;

                    if (doc == null) {
                        return null;
                    }

                    String estado = bucket.key().stringValue();
                    return new TopAvistamento(estado, doc);
                })
                .filter(Objects::nonNull)
                .toList();
    }
    public List<EstadoConfiabilidadeDto> getRankingSemanaAnterior(String dataBaseIso) throws IOException {
        List<TopAvistamento> tops = topSemanaAnterior(dataBaseIso);
        List<EstadoConfiabilidadeDto> estados = tops.stream()
                .map(t -> new EstadoConfiabilidadeDto(
                        t.estado(),
                        t.avistamento() != null ? t.avistamento().getConfiabilidade() : null
                ))
                .toList();
        return estados;

    }
    public String getInicioSemana(String dataBaseIso){
        LocalDate dataBase = LocalDate.parse(dataBaseIso);

        // Pega segunda-feira da semana da data recebida
        LocalDate segundaDaSemana = dataBase.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        // Agora vamos para a semana anterior
        LocalDate inicioSemanaAnterior = segundaDaSemana.minusWeeks(1);
        return inicioSemanaAnterior.toString();

    }
    public double calcularMediaConfiabilidade() throws IOException {
        SearchResponse<Void> response = esClient.search(s -> s
                        .index(INDEX)
                        .size(0) // não precisamos de documentos, só da agregação
                        .aggregations("media_confiabilidade", agg -> agg
                                .avg(a -> a.field("confiabilidade"))
                        ),
                Void.class
        );

        // Recupera a agregação
        Double valor = response.aggregations()
                .get("media_confiabilidade")
                .avg()
                .value();

        if (valor == null) {
            return 0.0;
        }

        return valor;
    }
    public EstatisticasConfiabilidade calcularEstatisticasConfiabilidade() throws IOException {

        SearchResponse<Void> response = esClient.search(s -> s
                        .index(INDEX)
                        .size(0)
                        .aggregations("media_confiabilidade", agg -> agg
                                .avg(a -> a.field("confiabilidade"))
                        )
                        .aggregations("stats_confiabilidade", agg -> agg
                                .extendedStats(a -> a.field("confiabilidade"))
                        )
                        .aggregations("alta_confiabilidade", agg -> agg
                                .filter(f -> f
                                        .range(r -> r.number(m -> m
                                                        .field("confiabilidade")
                                                        .gte(70D)
                                                )
                                        )
                                )
                        ),
                Void.class
        );

        // Média
        Double media = response.aggregations()
                .get("media_confiabilidade")
                .avg()
                .value();

        // Desvio-padrão
        Double stddev = response.aggregations()
                .get("stats_confiabilidade")
                .extendedStats()
                .stdDeviation();

        // Casos >= 60
        long alta = response.aggregations()
                .get("alta_confiabilidade")
                .filter()
                .docCount();

        return new EstatisticasConfiabilidade(
                media != null ? media : 0.0,
                stddev != null ? stddev : 0.0,
                alta
        );
    }
}
