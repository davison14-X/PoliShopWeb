package co.edu.pcjic.polishop.service;

import co.edu.pcjic.polishop.dto.EmprendimientoDTO;
import co.edu.pcjic.polishop.dto.EmprendimientoForm;
import co.edu.pcjic.polishop.dto.HorarioDTO;
import co.edu.pcjic.polishop.model.*;
import co.edu.pcjic.polishop.repository.*;
import co.edu.pcjic.polishop.util.HorarioUtil;
import co.edu.pcjic.polishop.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmprendimientoService {

    private static final String[] NOMBRES_DIAS = {
        "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"
    };

    private final EmprendimientoRepository empRepo;
    private final HorarioRepository horarioRepo;
    private final CategoriaRepository categoriaRepo;
    private final MeGustaRepository meGustaRepo;
    private final CloudinaryService cloudinaryService;

    @org.springframework.beans.factory.annotation.Value("${cloudinary.carpeta.logos}")
    private String carpetaLogos;

    @org.springframework.beans.factory.annotation.Value("${cloudinary.carpeta.portadas}")
    private String carpetaPortadas;

    @Transactional(readOnly = true)
    public List<EmprendimientoDTO> buscar(String q, Long categoriaId, String ubicacion) {
        List<Emprendimiento> lista;

        if (q != null && !q.isBlank()) {
            lista = empRepo.buscarPorTexto(q.trim() + "*");
        } else {
            lista = empRepo.findAllByActivoTrue();
        }

        return lista.stream()
                .filter(e -> categoriaId == null || (e.getCategoria() != null && e.getCategoria().getId().equals(categoriaId)))
                .filter(e -> {
                    if (ubicacion == null || ubicacion.isBlank()) return true;
                    if ("virtual".equalsIgnoreCase(ubicacion)) return e.isEsVirtual();
                    if ("campus".equalsIgnoreCase(ubicacion)) return !e.isEsVirtual();
                    return true;
                })
                .map(e -> toDTO(e, horarioRepo.findByEmprendimientoIdOrderByDiaSemana(e.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public EmprendimientoDTO findDTOBySlug(String slug) {
        Emprendimiento emp = empRepo.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Emprendimiento no encontrado"));
        List<Horario> horarios = horarioRepo.findByEmprendimientoIdOrderByDiaSemana(emp.getId());
        return toDTO(emp, horarios);
    }

    public Emprendimiento findBySlug(String slug) {
        return empRepo.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Emprendimiento no encontrado"));
    }

    public Emprendimiento findById(Long id) {
        return empRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Emprendimiento no encontrado"));
    }

    public Emprendimiento findByUsuario(Usuario usuario) {
        return empRepo.findByUsuarioId(usuario.getId()).stream().findFirst().orElse(null);
    }

    /**
     * Crea o actualiza el emprendimiento y sus horarios.
     * La validación del horario ocurre antes de cualquier operación de escritura
     * para evitar imágenes huérfanas en Cloudinary si la validación falla.
     *
     * @throws IllegalArgumentException si algún horario tiene la apertura >= cierre
     */
    @Transactional
    public Emprendimiento guardar(EmprendimientoForm form, Usuario usuario) throws IOException {
        // 1. Validar horarios primero, antes de subir imágenes o guardar en BD
        validarHorarios(form.getHorario());

        Emprendimiento emp = empRepo.findByUsuarioId(usuario.getId())
                .stream().findFirst()
                .orElseGet(() -> {
                    Emprendimiento nuevo = new Emprendimiento();
                    nuevo.setUsuario(usuario);
                    return nuevo;
                });

        emp.setNombre(form.getNombre());
        emp.setDescripcion(form.getDescripcion());
        emp.setEsVirtual(form.isEsVirtual());
        emp.setUbicacion(form.getUbicacion());
        emp.setActivo(true);

        if (emp.getSlug() == null) {
            emp.setSlug(SlugUtil.uniqueSlugEmprendimiento(form.getNombre(), empRepo));
        }

        Categoria categoria = categoriaRepo.findById(form.getCategoriaId())
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));
        emp.setCategoria(categoria);

        if (form.getLogo() != null && !form.getLogo().isEmpty()) {
            String url = cloudinaryService.subirImagen(form.getLogo(), carpetaLogos);
            emp.setLogoUrl(url);
        }

        if (form.getPortada() != null && !form.getPortada().isEmpty()) {
            String url = cloudinaryService.subirImagen(form.getPortada(), carpetaPortadas);
            emp.setPortadaUrl(url);
        }

        actualizarContacto(emp, Contacto.Tipo.WHATSAPP, form.getWhatsapp());
        actualizarContacto(emp, Contacto.Tipo.INSTAGRAM, form.getInstagram());

        Emprendimiento saved = empRepo.save(emp);

        // 2. Persistir horarios (ya validados)
        if (form.getHorario() != null) {
            for (Map.Entry<Integer, EmprendimientoForm.HorarioEntrada> entry : form.getHorario().entrySet()) {
                int dia = entry.getKey();
                EmprendimientoForm.HorarioEntrada h = entry.getValue();
                LocalTime apertura = parseTime(h.getHoraApertura());
                LocalTime cierre   = parseTime(h.getHoraCierre());

                Horario horario = horarioRepo.findByEmprendimientoIdAndDiaSemana(saved.getId(), dia)
                        .orElse(null);

                if (horario == null) {
                    if (apertura == null) continue;
                    horario = new Horario();
                    horario.setEmprendimiento(saved);
                    horario.setDiaSemana(dia);
                }

                horario.setActivo(h.isActivo());
                if (apertura != null) horario.setHoraApertura(apertura);
                if (cierre   != null) horario.setHoraCierre(cierre);
                horarioRepo.save(horario);
            }
        }

        return saved;
    }

    /**
     * Valida que ningún día con ambas horas definidas tenga apertura >= cierre.
     * Se aplica a todos los días (activos o no) para respetar el constraint de BD.
     *
     * @throws IllegalArgumentException con mensaje descriptivo si hay errores
     */
    private void validarHorarios(Map<Integer, EmprendimientoForm.HorarioEntrada> horario) {
        if (horario == null) return;

        List<String> errores = new ArrayList<>();
        for (Map.Entry<Integer, EmprendimientoForm.HorarioEntrada> entry : horario.entrySet()) {
            int dia = entry.getKey();
            EmprendimientoForm.HorarioEntrada h = entry.getValue();
            LocalTime apertura = parseTime(h.getHoraApertura());
            LocalTime cierre   = parseTime(h.getHoraCierre());
            if (apertura != null && cierre != null && !apertura.isBefore(cierre)) {
                String nombre = (dia >= 1 && dia <= 7) ? NOMBRES_DIAS[dia - 1] : "Día " + dia;
                errores.add(nombre + ": la apertura (" + h.getHoraApertura()
                        + ") debe ser anterior al cierre (" + h.getHoraCierre() + ")");
            }
        }
        if (!errores.isEmpty()) {
            throw new IllegalArgumentException(
                    "Horario inválido — " + String.join("; ", errores) + ". Corrige las horas e inténtalo de nuevo.");
        }
    }

    private LocalTime parseTime(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return LocalTime.parse(s, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            return null;
        }
    }

    @Transactional
    public void toggleFlash(Long empId) {
        Emprendimiento emp = empRepo.findById(empId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (HorarioUtil.esFlashActivo(emp)) {
            emp.setDisponibleHasta(null);
        } else {
            emp.setDisponibleHasta(LocalDateTime.now().plusHours(3));
        }
        empRepo.save(emp);
    }

    /**
     * Construye la lista de 7 días de la semana con su estado para la vista pública/panel.
     * Siempre devuelve los 7 días (Lunes–Domingo) aunque no haya datos guardados.
     */
    public List<HorarioDTO> buildHorarioDTOs(List<Horario> horarios) {
        int hoy = LocalDate.now().getDayOfWeek().getValue();
        Map<Integer, Horario> porDia = horarios.stream()
                .collect(Collectors.toMap(Horario::getDiaSemana, Function.identity()));

        return java.util.stream.IntStream.rangeClosed(1, 7)
                .mapToObj(dia -> {
                    Horario h = porDia.get(dia);
                    return HorarioDTO.builder()
                            .diaSemana(dia)
                            .nombre(NOMBRES_DIAS[dia - 1])
                            .activo(h != null && h.isActivo())
                            .horaApertura(h != null ? h.getHoraApertura() : null)
                            .horaCierre(h != null ? h.getHoraCierre() : null)
                            .esHoy(dia == hoy)
                            .build();
                })
                .toList();
    }

    public EmprendimientoDTO toDTO(Emprendimiento emp, List<Horario> horarios) {
        long likes = meGustaRepo.countByEmprendimientoId(emp.getId());
        boolean abierto = HorarioUtil.estaAbierto(emp, horarios);
        boolean flash = HorarioUtil.esFlashActivo(emp);

        return EmprendimientoDTO.builder()
                .id(emp.getId())
                .nombre(emp.getNombre())
                .slug(emp.getSlug())
                .descripcion(emp.getDescripcion())
                .logoUrl(emp.getLogoUrl())
                .portadaUrl(emp.getPortadaUrl())
                .categoriaId(emp.getCategoria() != null ? emp.getCategoria().getId() : null)
                .categoriaNombre(emp.getCategoria() != null ? emp.getCategoria().getNombre() : null)
                .categoriaIcono(emp.getCategoria() != null ? emp.getCategoria().getIcono() : null)
                .ubicacion(emp.getUbicacion())
                .esVirtual(emp.isEsVirtual())
                .activo(emp.isActivo())
                .abierto(abierto)
                .flashActivo(flash)
                .totalMeGusta(likes)
                .creadoEn(emp.getCreadoEn())
                .build();
    }

    private void actualizarContacto(Emprendimiento emp, Contacto.Tipo tipo, String valor) {
        emp.getContactos().removeIf(c -> c.getTipo() == tipo);
        if (valor != null && !valor.isBlank()) {
            Contacto c = new Contacto();
            c.setEmprendimiento(emp);
            c.setTipo(tipo);
            c.setValor(valor.trim());
            c.setPrincipal(true);
            emp.getContactos().add(c);
        }
    }
}
