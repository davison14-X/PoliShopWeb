package co.edu.pcjic.polishop.controller;

import co.edu.pcjic.polishop.dto.RegistroForm;
import co.edu.pcjic.polishop.model.Usuario;
import co.edu.pcjic.polishop.service.UsuarioService;
import co.edu.pcjic.polishop.service.VerificacionService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarioService;
    private final VerificacionService verificacionService;

    // ── Login ────────────────────────────────────────────────
    @GetMapping("/login")
    public String loginForm(@RequestParam(required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", "Correo o contraseña incorrectos, o cuenta no verificada.");
        }
        return "auth/login";
    }

    // ── Registro ─────────────────────────────────────────────
    @GetMapping("/registro")
    public String registroForm(Model model) {
        model.addAttribute("registroForm", new RegistroForm());
        return "auth/registro";
    }

    @PostMapping("/registro")
    public String registrar(@Valid @ModelAttribute RegistroForm registroForm,
                            BindingResult result,
                            Model model,
                            HttpSession session) {
        if (result.hasErrors()) {
            model.addAttribute("errores", result.getAllErrors().stream()
                    .map(e -> e.getDefaultMessage()).toList());
            return "auth/registro";
        }

        try {
            Usuario usuario = usuarioService.registrar(registroForm);
            verificacionService.generarYEnviarCodigo(usuario);
            session.setAttribute("correoVerificacion", usuario.getCorreoInstitucional());
            return "redirect:/auth/verificar";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errores", java.util.List.of(ex.getMessage()));
            return "auth/registro";
        }
    }

    // ── Verificación ──────────────────────────────────────────
    @GetMapping("/verificar")
    public String verificarForm(HttpSession session, Model model) {
        String correo = (String) session.getAttribute("correoVerificacion");
        if (correo == null) return "redirect:/auth/login";
        model.addAttribute("correo", correo);
        return "auth/verificar";
    }

    @PostMapping("/verificar")
    public String verificar(@RequestParam String codigo,
                            HttpSession session,
                            Model model) {
        String correo = (String) session.getAttribute("correoVerificacion");
        if (correo == null) return "redirect:/auth/login";

        boolean ok = verificacionService.validarCodigo(correo, codigo);

        if (!ok) {
            model.addAttribute("correo", correo);
            model.addAttribute("error", "Código incorrecto, expirado o sin intentos disponibles.");
            return "auth/verificar";
        }

        // Login automático
        UserDetails userDetails = usuarioService.loadUserByUsername(correo);
        var auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
        session.removeAttribute("correoVerificacion");

        return "redirect:/panel";
    }

    // ── Reenviar código ───────────────────────────────────────
    @PostMapping("/reenviar-codigo")
    public String reenviarCodigo(HttpSession session, RedirectAttributes ra) {
        String correo = (String) session.getAttribute("correoVerificacion");
        if (correo == null) return "redirect:/auth/login";

        try {
            verificacionService.reenviarCodigo(correo);
            ra.addFlashAttribute("exito", "Código reenviado a " + correo);
        } catch (Exception e) {
            ra.addFlashAttribute("error", "No se pudo reenviar el código.");
        }
        return "redirect:/auth/verificar";
    }
}
