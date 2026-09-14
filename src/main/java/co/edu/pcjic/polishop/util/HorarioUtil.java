package co.edu.pcjic.polishop.util;

import co.edu.pcjic.polishop.model.Emprendimiento;
import co.edu.pcjic.polishop.model.Horario;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Utilidades estáticas para evaluar el estado de apertura de un emprendimiento.
 * La lógica de "flash" tiene prioridad sobre el horario regular.
 */
public final class HorarioUtil {

    private HorarioUtil() {}

    /**
     * Determina si el emprendimiento está abierto en este momento.
     * Primero verifica el modo flash (venta inmediata); si no está activo,
     * evalúa el horario configurado para el día actual.
     */
    public static boolean estaAbierto(Emprendimiento emp, List<Horario> horarios) {
        LocalDateTime ahora = LocalDateTime.now();

        if (esFlashActivo(emp, ahora)) {
            return true;
        }

        int hoy = LocalDate.now().getDayOfWeek().getValue(); // 1=Lunes … 7=Domingo
        LocalTime horaActual = ahora.toLocalTime();

        return horarios.stream()
                .filter(h -> h.getDiaSemana() == hoy && h.isActivo())
                .anyMatch(h -> h.getHoraApertura() != null
                        && h.getHoraCierre() != null
                        && !horaActual.isBefore(h.getHoraApertura())
                        && !horaActual.isAfter(h.getHoraCierre()));
    }

    /** Devuelve {@code true} si el modo flash está activo en este momento. */
    public static boolean esFlashActivo(Emprendimiento emp) {
        return esFlashActivo(emp, LocalDateTime.now());
    }

    private static boolean esFlashActivo(Emprendimiento emp, LocalDateTime ahora) {
        return emp.getDisponibleHasta() != null && emp.getDisponibleHasta().isAfter(ahora);
    }
}
