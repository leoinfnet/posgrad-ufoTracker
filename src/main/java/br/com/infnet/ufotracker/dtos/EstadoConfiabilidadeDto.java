package br.com.infnet.ufotracker.dtos;

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