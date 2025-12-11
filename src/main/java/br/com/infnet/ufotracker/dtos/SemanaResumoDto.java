package br.com.infnet.ufotracker_gab.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SemanaResumoDto {
    private String semana; // ex: "2025-03-24 a 2025-03-30"
    private List<EstadoConfiabilidadeDto> estados;
}
