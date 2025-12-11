package br.com.infnet.ufotracker.repository;

import br.com.infnet.ufotracker.model.Avistamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

public interface AvistamentoRepository extends JpaRepository<Avistamento, UUID> {

}
