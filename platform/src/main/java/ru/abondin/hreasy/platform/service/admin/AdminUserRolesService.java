package ru.abondin.hreasy.platform.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.api.admin.AdminUserRolesController;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.employee.EmployeeRepo;
import ru.abondin.hreasy.platform.repo.employee.admin.SecAdminUserRolesRepo;
import ru.abondin.hreasy.platform.repo.employee.admin.UserRoleHistoryEntry;
import ru.abondin.hreasy.platform.repo.employee.admin.UserRoleHistoryRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.admin.dto.UserSecurityInfoDto;
import ru.abondin.hreasy.platform.service.admin.dto.UserSecurityInfoMapper;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminUserRolesService {

    private final EmployeeRepo employeeRepo;
    private final UserSecurityInfoMapper mapper;
    private final DateTimeService dateTimeService;
    private final AdminSecurityValidator securityValidator;
    private final UserRoleHistoryRepo historyRepo;

    private final SecAdminUserRolesRepo adminUserRolesRepo;


    public Flux<UserSecurityInfoDto> users(AuthContext auth, @Nullable AdminUserRolesController.AdminUserListFilter filter) {
        var now = dateTimeService.now();
        return securityValidator.validateGetEmployeeWithSecurity(auth).flatMapMany(b ->
                employeeRepo.findWithSecurityInfo().map(mapper::fromEntry).filter(employee -> {
                    if (filter != null && !filter.isIncludeFired()) {
                        return employee.getDateOfDismissal() == null || employee.getDateOfDismissal().isAfter(now.toLocalDate());
                    }
                    return true;
                }));
    }

    @Transactional
    public Mono<Integer> updateRoles(AuthContext auth, int employeeId, AdminUserRolesController.UserRolesUpdateBody body) {
        log.info("Updating employee {} by {} with {}", employeeId, auth.getUsername(), body);
        return securityValidator.validateUpdateUserRoles(auth)
                .flatMap(b -> doUpdate(employeeId, auth.getEmployeeInfo().getEmployeeId(), body));
    }

    private Mono<Integer> doUpdate(int employeeId, int loggedInEmployeeId, AdminUserRolesController.UserRolesUpdateBody body) {
        var now = dateTimeService.now();
        var history = new UserRoleHistoryEntry();
        history.setRoles(StringUtils.join(body.getRoles(), ','));
        history.setAccessibleDepartments(StringUtils.join(body.getAccessibleDepartments(), ','));
        history.setAccessibleProjects(StringUtils.join(body.getAccessibleProjects(), ','));
        history.setEmployeeId(employeeId);
        history.setUpdatedAt(now);
        history.setUpdatedBy(loggedInEmployeeId);
        return
                // 1. Save history
                historyRepo.save(history)
                        // 2. Update roles
                        .then(adminUserRolesRepo.updateRoles(employeeId, body.getRoles()))
                        // 3. Update accessible departments
                        .then(adminUserRolesRepo.updateAccessibleDepartments(employeeId, body.getAccessibleDepartments()))
                        // 4. Update accessible projects
                        .then(adminUserRolesRepo.updateAccessibleProjects(employeeId, body.getAccessibleProjects()))
                        .then(Mono.just(employeeId));
    }


}
