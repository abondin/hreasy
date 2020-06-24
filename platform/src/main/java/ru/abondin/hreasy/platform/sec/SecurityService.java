package ru.abondin.hreasy.platform.sec;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.repo.employee.EmployeeRepo;
import ru.abondin.hreasy.platform.repo.sec.PermissionRepo;
import ru.abondin.hreasy.platform.repo.sec.UserEntry;
import ru.abondin.hreasy.platform.repo.sec.UserRepo;

import java.util.Arrays;

@Component
@Slf4j
@RequiredArgsConstructor
public class SecurityService {
    private final UserRepo userRepo;
    private final EmployeeRepo employeeRepo;
    private final PermissionRepo permissionRepo;

    @Transactional
    public Flux<String> getPermissions(String email) {
        return getUserIdOrCreateUserFromEmployee(email)
                .flatMapMany(userId -> permissionRepo.findByUserId(userId))
                .map(perm -> perm.getPermission());
    }


    /**
     * Load user from database. Create based on employee information if doesn't exists
     *
     * @return userId if found
     */
    private Mono<Integer> getUserIdOrCreateUserFromEmployee(String email) {
        return userRepo.findIdByEmail(email)
                .switchIfEmpty(
                        // Create security user from employee information
                        employeeRepo.findIdByEmail(email).flatMap(
                                employeeId -> createUser(email, employeeId))
                                .switchIfEmpty(Mono.error(new BusinessError("errors.no.employee.found", Arrays.asList(email)))));
    }

    private Mono<Integer> createUser(String email, int employeeId) {
        log.info("Creating new security user for employee " + employeeId + ":" + email);
        var userEntry = new UserEntry(null, email, employeeId);
        return userRepo.save(userEntry).map(u -> u.getId());
    }
}
