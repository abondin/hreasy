package ru.abondin.hreasy.platform.repo.employee;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.repo.sec.SecPasswdEntry;
import ru.abondin.hreasy.platform.repo.sec.SecPasswdRepo;

/**
 * Simple aggregation for basic {@link EmployeeRepo} methods to provide security information for given employee
 */
@Service
@RequiredArgsConstructor
public class EmployeeAuthDomainService {

    private final EmployeeRepo employeeRepo;
    private final SecPasswdRepo passwdRepo;

    public Mono<String> findEmailByTelegramAccount(String telegramAccount) {
        return employeeRepo.findEmailByTelegramAccount(telegramAccount)
                .map(EmployeeRepo.EmployeeTelegramBinding::getEmail);
    }

    public Mono<EmployeeAuthInfoEntry> findIdByEmail(String email) {
        return employeeRepo.findIdByEmail(email).flatMap(empl -> {
            var entry = new EmployeeAuthInfoEntry();
            entry.setId(empl.getId());
            entry.setDepartmentId(empl.getDepartmentId());
            entry.setCurrentProjectId(empl.getCurrentProjectId());
            return employeeRepo.findAccessibleDepartments(empl.getId()).collectList()
                    .flatMap(deps -> employeeRepo.findAccessibleProjects(empl.getId()).collectList()
                            .flatMap(projects -> employeeRepo.findAccessibleBas(empl.getId()).collectList()
                                    .map(bas -> {
                                        entry.setAccessibleDepartments(deps);
                                        entry.setAccessibleProjects(projects);
                                        entry.setAccessibleBas(bas);
                                        return entry;
                                    })));
        });
    }

    public Mono<String> getInternalPassword(int employeeId) {
        return passwdRepo.findByEmployee(employeeId).map(SecPasswdEntry::getPasswordHash);
    }
}
