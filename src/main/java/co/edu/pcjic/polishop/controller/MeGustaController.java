package co.edu.pcjic.polishop.controller;

import co.edu.pcjic.polishop.model.Emprendimiento;
import co.edu.pcjic.polishop.model.MeGusta;
import co.edu.pcjic.polishop.model.Producto;
import co.edu.pcjic.polishop.model.Usuario;
import co.edu.pcjic.polishop.repository.MeGustaRepository;
import co.edu.pcjic.polishop.service.EmprendimientoService;
import co.edu.pcjic.polishop.service.ProductoService;
import co.edu.pcjic.polishop.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/me-gusta")
@RequiredArgsConstructor
public class MeGustaController {

    private final MeGustaRepository meGustaRepo;
    private final EmprendimientoService emprendimientoService;
    private final ProductoService productoService;
    private final UsuarioService usuarioService;

    @Transactional
    @PostMapping("/emprendimiento/{id}")
    public ResponseEntity<?> toggleEmprendimiento(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Usuario usuario = usuarioService.findByCorreo(userDetails.getUsername());
        Emprendimiento emp = emprendimientoService.findById(id);

        boolean yaExiste = meGustaRepo.existsByUsuarioIdAndEmprendimientoId(usuario.getId(), id);
        if (yaExiste) {
            meGustaRepo.deleteByUsuarioIdAndEmprendimientoId(usuario.getId(), id);
        } else {
            MeGusta mg = new MeGusta();
            mg.setUsuario(usuario);
            mg.setEmprendimiento(emp);
            meGustaRepo.save(mg);
        }

        long total = meGustaRepo.countByEmprendimientoId(id);
        return ResponseEntity.ok(Map.of("liked", !yaExiste, "likes", total));
    }

    @Transactional
    @PostMapping("/producto/{id}")
    public ResponseEntity<?> toggleProducto(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Usuario usuario = usuarioService.findByCorreo(userDetails.getUsername());
        Producto producto = productoService.findById(id);

        boolean yaExiste = meGustaRepo.existsByUsuarioIdAndProductoId(usuario.getId(), id);
        if (yaExiste) {
            meGustaRepo.deleteByUsuarioIdAndProductoId(usuario.getId(), id);
        } else {
            MeGusta mg = new MeGusta();
            mg.setUsuario(usuario);
            mg.setProducto(producto);
            meGustaRepo.save(mg);
        }

        long total = meGustaRepo.countByProductoId(id);
        return ResponseEntity.ok(Map.of("liked", !yaExiste, "likes", total));
    }
}
