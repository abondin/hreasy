package ru.abondin.hreasy.platform.repo.employee;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Simple aggregation for basic {@link EmployeeRepo} methods to provide security information for given employee
 */
@Service
@RequiredArgsConstructor
public class EmployeeAuthDomainService {

    private final EmployeeRepo employeeRepo;

    public Mono<EmployeeAuthInfoEntry> findIdByEmail(String email) {
        return employeeRepo.findIdByEmail(email).flatMap(id -> {
            var entry = new EmployeeAuthInfoEntry();
            entry.setId(id);
            return employeeRepo.findAccessibleDepartments(id).collectList()
                    .flatMap(deps -> employeeRepo.findAccessibleProjects(id).collectList()
                            .map(projects -> {
                                entry.setAccessibleDepartments(deps);
                                entry.setAccessibleProjects(projects);
                                return entry;
                            }));
        });
    }
}
