package co.edu.pcjic.polishop.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class ProductoDTO {

    private Long id;
    private String nombre;
    private String slug;
    private String descripcion;
    private BigDecimal precio;
    private boolean disponible;
    private String imagenPrincipal;
    private List<String> imagenesAdicionales;
    private String emprendimientoNombre;
    private String emprendimientoSlug;
    private String logoEmprendimiento;
    private long totalMeGusta;
    private boolean meGusta;
}
