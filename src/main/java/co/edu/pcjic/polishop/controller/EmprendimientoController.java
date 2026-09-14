package co.edu.pcjic.polishop.controller;

import co.edu.pcjic.polishop.model.Contacto;
import co.edu.pcjic.polishop.model.Emprendimiento;
import co.edu.pcjic.polishop.model.Usuario;
import co.edu.pcjic.polishop.repository.HorarioRepository;
import co.edu.pcjic.polishop.repository.MeGustaRepository;
import co.edu.pcjic.polishop.service.EmprendimientoService;
import co.edu.pcjic.polishop.service.ProductoService;
import co.edu.pcjic.polishop.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class EmprendimientoController {

    private final EmprendimientoService emprendimientoService;
    private final ProductoService productoService;
    private final HorarioRepository horarioRepo;
    private final MeGustaRepository meGustaRepo;
    private final UsuarioService usuarioService;

    @Transactional(readOnly = true)
    @GetMapping("/catalogo/{slug}")
    public String detalle(@PathVariable String slug,
                          @AuthenticationPrincipal UserDetails userDetails,
                          Model model) {

        Emprendimiento emp = emprendimientoService.findBySlug(slug);
        var horarios = horarioRepo.findByEmprendimientoIdOrderByDiaSemana(emp.getId());

        String contactoWa = emp.getContactos().stream()
                .filter(c -> c.getTipo() == Contacto.Tipo.WHATSAPP)
                .map(Contacto::getValor)
                .findFirst().orElse(null);

        String contactoIg = emp.getContactos().stream()
                .filter(c -> c.getTipo() == Contacto.Tipo.INSTAGRAM)
                .map(Contacto::getValor)
                .findFirst().orElse(null);

        boolean meGusta = false;
        if (userDetails != null) {
            Usuario usuario = usuarioService.findByCorreo(userDetails.getUsername());
            meGusta = meGustaRepo.existsByUsuarioIdAndEmprendimientoId(usuario.getId(), emp.getId());
        }

        model.addAttribute("emp", emprendimientoService.toDTO(emp, horarios));
        model.addAttribute("productos", productoService.listarDisponiblesPorEmprendimiento(emp.getId()));
        model.addAttribute("horarios", emprendimientoService.buildHorarioDTOs(horarios));
        model.addAttribute("contactoWa", contactoWa);
        model.addAttribute("contactoIg", contactoIg);
        model.addAttribute("meGusta", meGusta);
        model.addAttribute("paginaActual", "catalogo");

        return "catalogo/detalle";
    }
}
