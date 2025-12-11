package br.com.infnet.ufotracker_gab.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EstadoConfiabilidadeDto {
    private String estado;
    private Integer confiabilidade;

}