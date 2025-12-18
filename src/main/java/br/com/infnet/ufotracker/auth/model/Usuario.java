package br.com.infnet.ufotracker.auth.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Entity
@Table(name = "usuarios", schema = "auth")
@Getter@Setter
public class Usuario {
    @Id@GeneratedValue
    private Long id;
    private String username;
    private String password;
    private boolean ativo;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "usuarios_roles",
            schema = "auth",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;
}
