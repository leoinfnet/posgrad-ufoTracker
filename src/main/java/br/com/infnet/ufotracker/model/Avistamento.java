package br.com.infnet.ufotracker.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity@NoArgsConstructor
@Table(name = "avistamentos")
@Getter@Setter
public class Avistamento {
    @Id@GeneratedValue
    private UUID id;

    @Column(nullable = false, name = "datahora")
    private OffsetDateTime dataHora;
    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal latitude;
    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(length = 20, name = "city", nullable = false)
    private String cidade;
    @Column(length = 2, nullable = false)
    private String estado;
    @Column(length = 20, name = "tipo_objeto", nullable = false)
    private String tipoObjeto;

    private String descricao;
    private Integer confiabilidade;

}
//POJO
//PLAIN OLD JAVA OBJECT
//OBJETO QUE TEM CONSTRUTOR PADRAO E GETTERs e SETTERs DOS CAMPOS
