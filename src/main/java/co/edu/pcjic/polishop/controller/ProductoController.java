package co.edu.pcjic.polishop.controller;

import co.edu.pcjic.polishop.dto.ProductoDTO;
import co.edu.pcjic.polishop.model.Contacto;
import co.edu.pcjic.polishop.model.Emprendimiento;
import co.edu.pcjic.polishop.model.Usuario;
import co.edu.pcjic.polishop.repository.MeGustaRepository;
import co.edu.pcjic.polishop.service.EmprendimientoService;
import co.edu.pcjic.polishop.service.ProductoService;
import co.edu.pcjic.polishop.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
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
public class ProductoController {

    private final ProductoService productoService;
    private final EmprendimientoService emprendimientoService;
    private final MeGustaRepository meGustaRepo;
    private final UsuarioService usuarioService;

    @Transactional(readOnly = true)
    @GetMapping("/p/{slug}")
    public String producto(@PathVariable String slug,
                           @AuthenticationPrincipal UserDetails userDetails,
                           HttpServletRequest request,
                           Model model) {

        ProductoDTO prod = productoService.findDTOBySlug(slug);
        Emprendimiento emp = emprendimientoService.findBySlug(prod.getEmprendimientoSlug());

        String contactoWa = emp.getContactos().stream()
                .filter(c -> c.getTipo() == Contacto.Tipo.WHATSAPP)
                .map(Contacto::getValor)
                .findFirst().orElse(null);

        boolean meGusta = false;
        if (userDetails != null) {
            Usuario usuario = usuarioService.findByCorreo(userDetails.getUsername());
            meGusta = meGustaRepo.existsByUsuarioIdAndProductoId(usuario.getId(), prod.getId());
        }

        int port = request.getServerPort();
        String urlCanonica = request.getScheme() + "://" + request.getServerName()
                + (port != 80 && port != 443 ? ":" + port : "")
                + "/p/" + slug;

        model.addAttribute("prod", prod);
        model.addAttribute("contactoWa", contactoWa);
        model.addAttribute("meGusta", meGusta);
        model.addAttribute("urlCanonica", urlCanonica);
        model.addAttribute("paginaActual", "catalogo");

        return "catalogo/producto";
    }
}
