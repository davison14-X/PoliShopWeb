package co.edu.pcjic.polishop.repository;

import co.edu.pcjic.polishop.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
}
