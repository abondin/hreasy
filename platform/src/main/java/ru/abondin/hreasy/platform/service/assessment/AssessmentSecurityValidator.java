package ru.abondin.hreasy.platform.service.assessment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthContext;

/**
 * Validate security rules to  update employee current project
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AssessmentSecurityValidator {

    public Mono<Boolean> validateCanCreateAssessment(AuthContext auth) {
        return Mono.defer(() -> {
            if (auth.getAuthorities().contains("create_assessment")) {
                return Mono.just(true);
            }
            return Mono.error(new AccessDeniedException("Only user with permission create_assessment" +
                    " can view short assessment info or create new assessment"));
        });
    }
}
