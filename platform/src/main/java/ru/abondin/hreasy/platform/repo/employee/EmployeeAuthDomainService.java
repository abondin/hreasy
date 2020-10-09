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
        return employeeRepo.findIdByEmail(email).flatMap(empl -> {
            var entry = new EmployeeAuthInfoEntry();
            entry.setId(empl.getId());
            entry.setDepartmentId(empl.getDepartmentId());
            entry.setCurrentProjectId(empl.getCurrentProjectId());
            return employeeRepo.findAccessibleDepartments(empl.getId()).collectList()
                    .flatMap(deps -> employeeRepo.findAccessibleProjects(empl.getId()).collectList()
                            .map(projects -> {
                                entry.setAccessibleDepartments(deps);
                                entry.setAccessibleProjects(projects);
                                return entry;
                            }));
        });
    }
}
