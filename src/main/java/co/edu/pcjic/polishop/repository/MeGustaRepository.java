package co.edu.pcjic.polishop.repository;

import co.edu.pcjic.polishop.model.MeGusta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeGustaRepository extends JpaRepository<MeGusta, Long> {

    boolean existsByUsuarioIdAndProductoId(Long usuarioId, Long productoId);

    boolean existsByUsuarioIdAndEmprendimientoId(Long usuarioId, Long emprendimientoId);

    long countByProductoId(Long productoId);

    long countByEmprendimientoId(Long emprendimientoId);

    void deleteByUsuarioIdAndProductoId(Long usuarioId, Long productoId);

    void deleteByUsuarioIdAndEmprendimientoId(Long usuarioId, Long emprendimientoId);
}
