package ru.abondin.hreasy.platform.service.assessment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthContext;

import java.util.List;

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

    public Mono<Boolean> validateOwnerOrCanViewAssessmentFull(AuthContext auth, List<Integer> assessmentOwners) {
        return Mono.defer(() -> {
            if (auth.getAuthorities().contains("view_assessment_full")) {
                return Mono.just(true);
            } else if (assessmentOwners.contains(auth.getEmployeeInfo().getEmployeeId())) {
                return Mono.just(true);
            }
            return Mono.error(new AccessDeniedException("Only user with permission view_assessment_full" +
                    " or assessment owner can see detailed information and update assessment"));
        });

    }

    public Mono<Boolean> validateExportAssessments(AuthContext auth) {
        return validateCanCreateAssessment(auth);
    }
}
