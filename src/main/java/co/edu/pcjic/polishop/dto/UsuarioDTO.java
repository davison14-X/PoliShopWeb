package co.edu.pcjic.polishop.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UsuarioDTO {

    private Long id;
    private String nombre;
    private String apellido;
    private String correoInstitucional;
}
