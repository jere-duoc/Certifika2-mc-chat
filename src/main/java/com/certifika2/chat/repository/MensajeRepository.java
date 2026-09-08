package com.certifika2.chat.repository;

import com.certifika2.chat.model.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MensajeRepository extends JpaRepository<Mensaje, Long> {
    List<Mensaje> findByIdCursoOrderByFechaEnvioAsc(Long idCurso);
}
