package ru.abondin.hreasy.platform.repo.employee.admin;


import reactor.core.publisher.Mono;

import java.util.List;


public interface SecAdminUserRolesRepo {

    Mono<Integer> updateRoles(int employeeId, List<String> roles);

    Mono<Integer> updateAccessibleDepartments(int employeeId, List<Integer> accessibleDepartments);

    Mono<Integer> updateAccessibleProjects(int employeeId, List<Integer> accessibleProjects);

    Mono<Integer> updateAccessibleBas(int employeeId, List<Integer> accessibleBas);

    Mono<Integer> addAccessibleProject(int employeeId, int projectId);
}
