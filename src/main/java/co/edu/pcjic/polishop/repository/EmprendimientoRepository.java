package co.edu.pcjic.polishop.repository;

import co.edu.pcjic.polishop.model.Emprendimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmprendimientoRepository extends JpaRepository<Emprendimiento, Long> {

    Optional<Emprendimiento> findBySlug(String slug);

    List<Emprendimiento> findAllByActivoTrue();

    List<Emprendimiento> findByUsuarioId(Long usuarioId);

    boolean existsBySlug(String slug);

    @Query(value = """
            SELECT * FROM emprendimiento
            WHERE activo = true
              AND MATCH(nombre, descripcion) AGAINST(:q IN BOOLEAN MODE)
            """, nativeQuery = true)
    List<Emprendimiento> buscarPorTexto(@Param("q") String q);
}
