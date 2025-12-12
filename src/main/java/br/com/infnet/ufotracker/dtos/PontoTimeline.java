package br.com.infnet.ufotracker.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PontoTimeline {
    private String x; // "00h", "03h", etc
    private long y;   // quantidade de avistamentos
}