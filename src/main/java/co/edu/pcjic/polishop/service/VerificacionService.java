package co.edu.pcjic.polishop.service;

import co.edu.pcjic.polishop.model.TokenVerificacion;
import co.edu.pcjic.polishop.model.Usuario;
import co.edu.pcjic.polishop.repository.TokenVerificacionRepository;
import co.edu.pcjic.polishop.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VerificacionService {

    private final TokenVerificacionRepository tokenRepo;
    private final UsuarioRepository usuarioRepo;
    private final JavaMailSender mailSender;

    @Value("${polishop.mail.remitente}")
    private String remitente;

    @Value("${polishop.verificacion.expiracion-minutos:15}")
    private int expiracionMinutos;

    @Value("${polishop.verificacion.max-intentos:3}")
    private int maxIntentos;

    @Transactional
    public void generarYEnviarCodigo(Usuario usuario) {
        // Invalidar tokens anteriores
        tokenRepo.deleteByUsuarioId(usuario.getId());

        String codigo = String.format("%06d", new SecureRandom().nextInt(1_000_000));

        TokenVerificacion token = new TokenVerificacion();
        token.setUsuario(usuario);
        token.setCodigo(codigo);
        token.setIntentos(0);
        token.setUsado(false);
        token.setExpiraEn(LocalDateTime.now().plusMinutes(expiracionMinutos));
        tokenRepo.save(token);

        enviarCorreo(usuario.getCorreoInstitucional(), codigo);
    }

    /**
     * Valida el código ingresado por el usuario.
     * Registra cada intento fallido y bloquea el token al alcanzar el máximo.
     * El usuario dispone de exactamente {@code maxIntentos} intentos para acertar.
     */
    @Transactional
    public boolean validarCodigo(String correo, String codigoIngresado) {
        Usuario usuario = usuarioRepo.findByCorreoInstitucional(correo.toLowerCase())
                .orElse(null);
        if (usuario == null) return false;

        TokenVerificacion token = tokenRepo
                .findTopByUsuarioIdAndUsadoFalseAndExpiraEnAfterOrderByCreadoEnDesc(
                        usuario.getId(), LocalDateTime.now())
                .orElse(null);

        if (token == null) return false;

        // Verificar que el token no haya alcanzado ya el límite de intentos
        if (token.getIntentos() >= maxIntentos) {
            token.setUsado(true);
            tokenRepo.save(token);
            return false;
        }

        // El código es incorrecto: registrar intento y bloquear si llega al límite
        if (!token.getCodigo().equals(codigoIngresado.trim())) {
            token.setIntentos(token.getIntentos() + 1);
            if (token.getIntentos() >= maxIntentos) {
                token.setUsado(true);
            }
            tokenRepo.save(token);
            return false;
        }

        // Código correcto: marcar token como usado y verificar la cuenta
        token.setUsado(true);
        tokenRepo.save(token);
        usuario.setVerificado(true);
        usuarioRepo.save(usuario);

        return true;
    }

    @Transactional
    public void reenviarCodigo(String correo) {
        Usuario usuario = usuarioRepo.findByCorreoInstitucional(correo.toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Correo no encontrado"));
        generarYEnviarCodigo(usuario);
    }

    private void enviarCorreo(String destinatario, String codigo) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(destinatario);
        mensaje.setSubject("PoliShop — Código de verificación");
        mensaje.setText("""
                Hola,

                Tu código de verificación para PoliShop es:

                    %s

                Este código expira en %d minutos.

                Si no solicitaste este código, ignora este mensaje.

                — PoliShop PCJIC
                """.formatted(codigo, expiracionMinutos));
        mailSender.send(mensaje);
    }
}
