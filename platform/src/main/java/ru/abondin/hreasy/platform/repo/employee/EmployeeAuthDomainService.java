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
            return Mono.zip(employeeRepo.findAccessibleDepartments(id).collectList(), employeeRepo.findAccessibleProjects(id).collectList()).map(result -> {
                entry.setAccessibleDepartments(result.getT1());
                entry.setAccessibleProjects(result.getT2());
                return entry;
            });
        });
    }
}
