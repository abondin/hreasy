package ru.abondin.hreasy.platform.service.udr;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.udr.UdrAccessRepo;

/**
 * Validate security rules to  update employee current project
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UdrSecurityValidator {

    private final UdrAccessRepo accessRepo;

    public Mono<Boolean> validateViewUdr(AuthContext auth, int udrId) {
        return accessRepo.hasAccessToUdr(auth.getEmployeeInfo().getEmployeeId(), udrId).flatMap(r -> {
            if (r) {
                return Mono.just(true);
            }
            return Mono.error(new AccessDeniedException("Only UDR owner or user with admin_udr can view UDR"));
        });
    }


}
