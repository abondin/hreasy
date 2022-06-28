package ru.abondin.hreasy.platform.service.currentproject;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.dict.DictProjectRepo;
import ru.abondin.hreasy.platform.sec.ProjectHierarchyAccessor;

import java.util.ArrayList;

/**
 * Validate security rules to  update employee current project
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmployeeProjectSecurityValidator {

    private final ProjectHierarchyAccessor projectHierarchyService;

    public Mono<Boolean> validateAddOrDeleteSkill(AuthContext auth, int employeeId) {
        return Mono.defer(() -> {
            if (employeeId == auth.getEmployeeInfo().getEmployeeId()) {
                return Mono.just(true);
            }
            if (!auth.getAuthorities().contains("edit_skills")) {
                return Mono.just(false);
            }
            return projectHierarchyService.isManager(auth, employeeId);
        }).flatMap(r -> {
            if (r) {
                return Mono.just(true);
            }
            return Mono.error(new AccessDeniedException("Only logged in user or user with permission edit_skills can update employee skills"));
        });
    }


    public Mono<Boolean> validateUpdateRating(AuthContext auth, int skillEmployeeId, Integer skillId) {
        // Anyone add or update rating for skill
        return Mono.just(true);
    }

}
