package br.com.infnet.ufotracker.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // se vier campo a mais, ignora
public class AvistamentoDoc {

    private String id;

    @JsonProperty("dataHora")
    private String dataHora; // pode usar OffsetDateTime se quiser, mas String já funciona

    private String cidade;
    private String estado;
    private Integer confiabilidade;

    @JsonProperty("tipoObjeto")
    private String tipoObjeto;

    private String descricao;

    private Location location;
    private Double score;
    private String descricaoFormatada;

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Location {
        private double lat;
        private double lon;
    }
}
