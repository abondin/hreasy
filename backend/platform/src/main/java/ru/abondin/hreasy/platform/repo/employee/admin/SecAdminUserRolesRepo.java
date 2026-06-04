package ru.abondin.hreasy.platform.repo.employee.admin;


import reactor.core.publisher.Mono;

import java.util.List;


public interface SecAdminUserRolesRepo {

    Mono<Long> updateRoles(int employeeId, List<String> roles);

    Mono<Long> updateAccessibleDepartments(int employeeId, List<Integer> accessibleDepartments);

    Mono<Long> updateAccessibleProjects(int employeeId, List<Integer> accessibleProjects);

    Mono<Long> updateAccessibleBas(int employeeId, List<Integer> accessibleBas);

    Mono<Long> addAccessibleProject(int employeeId, int projectId);
}
