package co.edu.pcjic.polishop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter @Setter @NoArgsConstructor
public class EmprendimientoForm {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150)
    private String nombre;

    @NotNull(message = "Selecciona una categoría")
    private Long categoriaId;

    @Size(max = 2000)
    private String descripcion;

    private boolean esVirtual = false;

    @Size(max = 200)
    private String ubicacion;

    private String whatsapp;

    private String instagram;

    private String logoActualUrl;

    private MultipartFile logo;

    private String portadaActualUrl;

    private MultipartFile portada;

    // Clave: diaSemana (1=Lunes … 7=Domingo)
    private Map<Integer, HorarioEntrada> horario = new LinkedHashMap<>();

    @Getter @Setter @NoArgsConstructor
    public static class HorarioEntrada {
        private boolean activo = false;
        private String horaApertura;
        private String horaCierre;
    }
}
