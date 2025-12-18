package br.com.infnet.ufotracker.auth.repository;

import br.com.infnet.ufotracker.auth.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String email);
}
