package co.edu.pcjic.polishop.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalTime;

@Getter
@Builder
public class HorarioDTO {

    private int diaSemana;
    private String nombre;
    private boolean activo;
    private LocalTime horaApertura;
    private LocalTime horaCierre;
    private boolean esHoy;
}
