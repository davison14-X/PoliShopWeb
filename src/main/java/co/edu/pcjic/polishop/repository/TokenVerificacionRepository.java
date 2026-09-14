package co.edu.pcjic.polishop.repository;

import co.edu.pcjic.polishop.model.TokenVerificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TokenVerificacionRepository extends JpaRepository<TokenVerificacion, Long> {

    Optional<TokenVerificacion> findTopByUsuarioIdAndUsadoFalseAndExpiraEnAfterOrderByCreadoEnDesc(
            Long usuarioId, LocalDateTime ahora);

    void deleteByUsuarioId(Long usuarioId);
}
