package br.com.infnet.ufotracker.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EstatisticasConfiabilidade {
    private double media;
    private double desvioPadrao;
    private long quantidadeAltaConfiabilidade;
}