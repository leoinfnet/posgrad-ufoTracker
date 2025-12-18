package br.com.infnet.ufotracker.auth.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "roles", schema = "auth")
@Getter@Setter
public class Role {
    @Id@GeneratedValue
    private Long id;
    private String nome;
}
