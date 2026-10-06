package co.edu.pcjic.polishop.service;

import co.edu.pcjic.polishop.dto.RegistroForm;
import co.edu.pcjic.polishop.model.Usuario;
import co.edu.pcjic.polishop.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepo;
    private final PasswordEncoder passwordEncoder;

    /**
     * Registra un nuevo usuario validando dominio institucional, unicidad del correo
     * y coincidencia de contraseñas. La contraseña se almacena como hash BCrypt.
     *
     * @throws IllegalArgumentException si alguna de las validaciones falla
     */
    @Transactional
    public Usuario registrar(RegistroForm form) {
        if (!form.getCorreoInstitucional().toLowerCase().endsWith("@elpoli.edu.co")) {
            throw new IllegalArgumentException("El correo debe ser institucional (@elpoli.edu.co)");
        }
        if (usuarioRepo.existsByCorreoInstitucional(form.getCorreoInstitucional())) {
            throw new IllegalArgumentException("Ya existe una cuenta con ese correo");
        }
        if (!form.getContrasena().equals(form.getConfirmarContrasena())) {
            throw new IllegalArgumentException("Las contraseñas no coinciden");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(form.getNombre().trim());
        usuario.setApellido(form.getApellido().trim());
        usuario.setCorreoInstitucional(form.getCorreoInstitucional().trim().toLowerCase());
        usuario.setContrasenaHash(passwordEncoder.encode(form.getContrasena()));
        usuario.setVerificado(false);

        return usuarioRepo.save(usuario);
    }

    /**
     * Implementación de {@link UserDetailsService} requerida por Spring Security.
     * Lanza {@link UsernameNotFoundException} si el correo no existe o la cuenta
     * no ha sido verificada por email; ambos casos producen el mensaje genérico
     * "Correo o contraseña incorrectos" en la vista (evita enumeración de cuentas).
     */
    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepo.findByCorreoInstitucional(correo.toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + correo));

        if (!usuario.isVerificado()) {
            throw new UsernameNotFoundException("Cuenta no verificada: " + correo);
        }

        return new User(
                usuario.getCorreoInstitucional(),
                usuario.getContrasenaHash(),
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    public Usuario findByCorreo(String correo) {
        return usuarioRepo.findByCorreoInstitucional(correo.toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + correo));
    }
}
