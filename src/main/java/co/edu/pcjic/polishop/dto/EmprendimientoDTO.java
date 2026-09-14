package co.edu.pcjic.polishop.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class EmprendimientoDTO {

    private Long id;
    private String nombre;
    private String slug;
    private String descripcion;
    private String logoUrl;
    private String portadaUrl;
    private Long categoriaId;
    private String categoriaNombre;
    private String categoriaIcono;
    private String ubicacion;
    private boolean esVirtual;
    private boolean activo;
    private boolean abierto;
    private boolean flashActivo;
    private long totalMeGusta;
    private LocalDateTime creadoEn;
}
