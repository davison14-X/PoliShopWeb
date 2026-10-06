package co.edu.pcjic.polishop.controller;

import co.edu.pcjic.polishop.dto.*;
import co.edu.pcjic.polishop.model.Emprendimiento;
import co.edu.pcjic.polishop.model.Usuario;
import co.edu.pcjic.polishop.model.Contacto;
import co.edu.pcjic.polishop.repository.CategoriaRepository;
import co.edu.pcjic.polishop.repository.HorarioRepository;
import co.edu.pcjic.polishop.service.EmprendimientoService;
import co.edu.pcjic.polishop.service.ProductoService;
import co.edu.pcjic.polishop.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/panel")
@RequiredArgsConstructor
public class PanelController {

    private final UsuarioService usuarioService;
    private final EmprendimientoService emprendimientoService;
    private final ProductoService productoService;
    private final CategoriaRepository categoriaRepo;
    private final HorarioRepository horarioRepo;

    // ── Panel principal ────────────────────────────────────────
    @Transactional(readOnly = true)
    @GetMapping
    public String panel(@AuthenticationPrincipal UserDetails userDetails,
                        @RequestParam(defaultValue = "perfil") String tab,
                        @ModelAttribute("exito") String exito,
                        @ModelAttribute("error") String error,
                        Model model) {

        Usuario usuario = usuarioService.findByCorreo(userDetails.getUsername());
        Emprendimiento emp = emprendimientoService.findByUsuario(usuario);

        model.addAttribute("usuario", UsuarioDTO.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .correoInstitucional(usuario.getCorreoInstitucional())
                .build());

        if (emp != null) {
            var horarios = horarioRepo.findByEmprendimientoIdOrderByDiaSemana(emp.getId());
            model.addAttribute("emp", emprendimientoService.toDTO(emp, horarios));
            model.addAttribute("horarios", emprendimientoService.buildHorarioDTOs(horarios));
            model.addAttribute("productos", productoService.listarPorEmprendimiento(emp.getId()));
            String wa = emp.getContactos().stream()
                    .filter(c -> c.getTipo() == Contacto.Tipo.WHATSAPP)
                    .map(Contacto::getValor).findFirst().orElse(null);
            String ig = emp.getContactos().stream()
                    .filter(c -> c.getTipo() == Contacto.Tipo.INSTAGRAM)
                    .map(Contacto::getValor).findFirst().orElse(null);
            model.addAttribute("contactoWa", wa);
            model.addAttribute("contactoIg", ig);
        } else {
            model.addAttribute("emp", null);
            model.addAttribute("productos", List.of());
            model.addAttribute("horarios", emprendimientoService.buildHorarioDTOs(List.of()));
        }

        model.addAttribute("categorias", categoriaRepo.findAll());
        model.addAttribute("tab", tab);
        model.addAttribute("paginaActual", "panel");

        return "panel/index";
    }

    // ── Guardar perfil del emprendimiento ──────────────────────
    @PostMapping("/perfil")
    public String guardarPerfil(@AuthenticationPrincipal UserDetails userDetails,
                                @Valid @ModelAttribute EmprendimientoForm emprendimientoForm,
                                BindingResult result,
                                RedirectAttributes ra) throws IOException {

        if (result.hasErrors()) {
            ra.addFlashAttribute("error", "Revisa los campos e inténtalo de nuevo.");
            return "redirect:/panel?tab=perfil";
        }

        Usuario usuario = usuarioService.findByCorreo(userDetails.getUsername());
        try {
            emprendimientoService.guardar(emprendimientoForm, usuario);
            ra.addFlashAttribute("exito", "Perfil actualizado correctamente.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/panel?tab=perfil";
    }

    // ── Flash / disponibilidad especial (toggle) ──────────────
    @PostMapping("/flash")
    public String toggleFlash(@AuthenticationPrincipal UserDetails userDetails, RedirectAttributes ra) {
        Usuario usuario = usuarioService.findByCorreo(userDetails.getUsername());
        Emprendimiento emp = emprendimientoService.findByUsuario(usuario);
        if (emp != null) {
            emprendimientoService.toggleFlash(emp.getId());
            ra.addFlashAttribute("exito", "Estado de venta flash actualizado.");
        }
        return "redirect:/panel";
    }

    // ── Nuevo producto ─────────────────────────────────────────
    @GetMapping("/producto/nuevo")
    public String nuevoProductoForm(Model model) {
        model.addAttribute("productoForm", new ProductoForm());
        model.addAttribute("modoEdicion", false);
        model.addAttribute("paginaActual", "panel");
        return "panel/producto-form";
    }

    @PostMapping("/producto/nuevo")
    public String crearProducto(@AuthenticationPrincipal UserDetails userDetails,
                                @Valid @ModelAttribute ProductoForm productoForm,
                                BindingResult result,
                                Model model,
                                RedirectAttributes ra) throws IOException {

        if (result.hasErrors()) {
            model.addAttribute("errores", result.getAllErrors().stream()
                    .map(e -> e.getDefaultMessage()).toList());
            model.addAttribute("modoEdicion", false);
            model.addAttribute("paginaActual", "panel");
            return "panel/producto-form";
        }

        Usuario usuario = usuarioService.findByCorreo(userDetails.getUsername());
        Emprendimiento emp = emprendimientoService.findByUsuario(usuario);
        productoService.guardar(productoForm, emp);
        ra.addFlashAttribute("exito", "Producto creado correctamente.");
        return "redirect:/panel?tab=productos";
    }

    // ── Editar producto ────────────────────────────────────────
    // La verificación de propiedad previene que un usuario edite productos ajenos (IDOR).
    @Transactional(readOnly = true)
    @GetMapping("/producto/{id}/editar")
    public String editarProductoForm(@PathVariable Long id,
                                     @AuthenticationPrincipal UserDetails userDetails,
                                     Model model) {
        Usuario usuario = usuarioService.findByCorreo(userDetails.getUsername());
        Emprendimiento emp = emprendimientoService.findByUsuario(usuario);
        if (emp == null) return "redirect:/panel";

        var prod = productoService.findByIdForOwner(id, emp.getId());
        ProductoForm form = new ProductoForm();
        form.setId(prod.getId());
        form.setNombre(prod.getNombre());
        form.setDescripcion(prod.getDescripcion());
        form.setPrecio(prod.getPrecio());
        form.setDisponible(prod.isDisponible());
        form.setImagenActualUrl(prod.getImagenes().isEmpty() ? null : prod.getImagenes().get(0).getUrl());

        model.addAttribute("productoForm", form);
        model.addAttribute("modoEdicion", true);
        model.addAttribute("paginaActual", "panel");
        return "panel/producto-form";
    }

    @PostMapping("/producto/{id}/editar")
    public String actualizarProducto(@PathVariable Long id,
                                     @AuthenticationPrincipal UserDetails userDetails,
                                     @Valid @ModelAttribute ProductoForm productoForm,
                                     BindingResult result,
                                     Model model,
                                     RedirectAttributes ra) throws IOException {

        productoForm.setId(id);

        if (result.hasErrors()) {
            model.addAttribute("errores", result.getAllErrors().stream()
                    .map(e -> e.getDefaultMessage()).toList());
            model.addAttribute("modoEdicion", true);
            model.addAttribute("paginaActual", "panel");
            return "panel/producto-form";
        }

        Usuario usuario = usuarioService.findByCorreo(userDetails.getUsername());
        Emprendimiento emp = emprendimientoService.findByUsuario(usuario);
        // Verifica propiedad antes de guardar (lanza 403 si no pertenece al usuario)
        if (emp == null || productoService.findByIdForOwner(id, emp.getId()) == null) {
            return "redirect:/panel";
        }
        productoService.guardar(productoForm, emp);
        ra.addFlashAttribute("exito", "Producto actualizado correctamente.");
        return "redirect:/panel?tab=productos";
    }

    // ── Eliminar producto ──────────────────────────────────────
    @PostMapping("/producto/{id}/eliminar")
    public String eliminarProducto(@PathVariable Long id,
                                   @AuthenticationPrincipal UserDetails userDetails,
                                   RedirectAttributes ra) {
        Usuario usuario = usuarioService.findByCorreo(userDetails.getUsername());
        Emprendimiento emp = emprendimientoService.findByUsuario(usuario);
        // Verifica propiedad antes de eliminar (lanza 403 si no pertenece al usuario)
        if (emp != null) {
            productoService.findByIdForOwner(id, emp.getId());
            productoService.eliminar(id);
            ra.addFlashAttribute("exito", "Producto eliminado.");
        }
        return "redirect:/panel?tab=productos";
    }
}
