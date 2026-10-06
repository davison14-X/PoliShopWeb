package co.edu.pcjic.polishop.repository;

import co.edu.pcjic.polishop.model.Horario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HorarioRepository extends JpaRepository<Horario, Long> {

    List<Horario> findByEmprendimientoIdOrderByDiaSemana(Long emprendimientoId);

    Optional<Horario> findByEmprendimientoIdAndDiaSemana(Long emprendimientoId, int diaSemana);
}
