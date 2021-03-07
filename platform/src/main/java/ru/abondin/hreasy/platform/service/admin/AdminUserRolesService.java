package ru.abondin.hreasy.platform.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.api.admin.AdminUserRolesController;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.employee.EmployeeRepo;
import ru.abondin.hreasy.platform.repo.employee.admin.UserRoleHistoryEntry;
import ru.abondin.hreasy.platform.repo.employee.admin.UserRoleHistoryRepo;
import ru.abondin.hreasy.platform.repo.sec.UserEntry;
import ru.abondin.hreasy.platform.repo.sec.UserRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.admin.dto.UserSecurityInfoDto;
import ru.abondin.hreasy.platform.service.admin.dto.UserSecurityInfoMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminUserRolesService {

    private final UserRepo userRepo;
    private final EmployeeRepo employeeRepo;
    private final UserSecurityInfoMapper mapper;
    private final DateTimeService dateTimeService;
    private final AdminSecurityValidator securityValidator;
    private final UserRoleHistoryRepo historyRepo;

    private final DatabaseClient dbClient;


    public Flux<UserSecurityInfoDto> users(AuthContext auth, @Nullable AdminUserRolesController.AdminUserListFilter filter) {
        var now = dateTimeService.now();
        return securityValidator.validateGetEmployeeWithSecurity(auth).flatMapMany(b ->
                employeeRepo.findWithSecurityInfo().map(mapper::fromEntry).filter(employee -> {
                    if (filter == null && !filter.isIncludeFired()) {
                        return employee.getDateOfDismissal() == null || employee.getDateOfDismissal().isBefore(now.toLocalDate());
                    }
                    return true;
                }));
    }

    @Transactional
    public Mono<Integer> updateRoles(AuthContext auth, int employeeId, AdminUserRolesController.UserRolesUpdateBody body) {
        log.info("Updating employee {} by {} with {}", employeeId, auth.getUsername(), body);
        return securityValidator.validateUpdateUserRoles(auth).flatMap(b ->
                employeeRepo.findEmailById(employeeId)
                        // 1. Get employee email
                        .switchIfEmpty(Mono.error(new BusinessError("errors.no.employee.found",
                                Arrays.asList(Integer.toString(employeeId)))))
                        .flatMap(email ->
                                //2. Find security user
                                userRepo.findIdByEmployeeId(employeeId)
                                        //3. Create user if not found
                                        .switchIfEmpty(userRepo.save(new UserEntry(null, email, employeeId)).map(UserEntry::getId))
                                        //4. Do update
                                        .flatMap(userId -> doUpdate(employeeId, userId, auth.getEmployeeInfo().getEmployeeId(), body))
                        )


        );
    }

    private Mono<Integer> doUpdate(int employeeId, int userId, int loggedInEmployeeId, AdminUserRolesController.UserRolesUpdateBody body) {
        var now = dateTimeService.now();
        var history = new UserRoleHistoryEntry();
        history.setRoles(StringUtils.join(body.getRoles(), ','));
        history.setAccessibleDepartments(StringUtils.join(body.getAccessibleDepartments(), ','));
        history.setAccessibleProjects(StringUtils.join(body.getAccessibleProjects(), ','));
        history.setUserId(userId);
        history.setEmployeeId(employeeId);
        history.setUpdatedAt(now);
        history.setUpdatedBy(loggedInEmployeeId);
        return
                // 1. Save history
                historyRepo.save(history)
                        // 2. Update roles
                        .then(doDeleteAndInsert("sec_user_role", "user_id", userId,
                                "role", body.getRoles()))
                        // 3. Update accessible departments
                        .then(doDeleteAndInsert("employee_accessible_departments", "employee_id", employeeId,
                                "department_id", body.getAccessibleDepartments()))
                        // 4. Update accessible projects
                        .then(doDeleteAndInsert("employee_accessible_projects", "employee_id", employeeId,
                                "project_id", body.getAccessibleProjects()))
                        .then(Mono.just(employeeId));
    }


    private <T> Mono<Integer> doDeleteAndInsert(String tableName,
                                                String idFieldName,
                                                int id,
                                                String valueFieldName,
                                                List<T> values) {
        var inserts = new ArrayList<Mono<Integer>>();
        for (var r : values) {
            var i = dbClient.insert()
                    .into(tableName)
                    .value(idFieldName, id).value(valueFieldName, r).fetch().rowsUpdated();
            inserts.add(i);
        }
        // Delete all rows
        return dbClient.execute("delete from " + tableName + " where " + idFieldName + "=:id")
                .bind("id", id)
                .fetch().rowsUpdated()
                // 2. Insert new roles
                .then(values.isEmpty() ? Mono.just(0) : Mono.zip(inserts, (a) -> a.length));
    }


}
