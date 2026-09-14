package co.edu.pcjic.polishop.repository;

import co.edu.pcjic.polishop.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    Optional<Producto> findBySlug(String slug);

    List<Producto> findByEmprendimientoId(Long emprendimientoId);

    List<Producto> findByEmprendimientoIdAndDisponibleTrue(Long emprendimientoId);

    boolean existsBySlug(String slug);

    Optional<Producto> findByIdAndEmprendimientoId(Long id, Long emprendimientoId);
}
