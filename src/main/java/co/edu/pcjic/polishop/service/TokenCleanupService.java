package co.edu.pcjic.polishop.service;

import co.edu.pcjic.polishop.repository.TokenVerificacionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupService {

    private final TokenVerificacionRepository tokenRepo;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void limpiarTokensExpirados() {
        LocalDateTime limite = LocalDateTime.now().minusDays(1);
        tokenRepo.deleteByExpiraEnBefore(limite);
        log.info("Limpieza de tokens expirados ejecutada (anteriores a {})", limite);
    }
}
