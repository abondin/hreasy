package ru.abondin.hreasy.platform.repo.employee.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SecAdminUserRolesRepoImpl implements SecAdminUserRolesRepo {
    private final R2dbcEntityTemplate dbTemplate;

    @Override
    public Mono<Integer> updateRoles(int userId, List<String> roles) {
        return doDeleteAndInsert("sec_user_role", "user_id", userId,
                "role", roles);
    }

    @Override
    public Mono<Integer> updateAccessibleDepartments(int employeeId, List<Integer> accessibleDepartments) {
        return doDeleteAndInsert("employee_accessible_departments", "employee_id", employeeId,
                "department_id", accessibleDepartments);
    }

    @Override
    public Mono<Integer> updateAccessibleProjects(int employeeId, List<Integer> accessibleProjects) {
        return doDeleteAndInsert("employee_accessible_projects", "employee_id", employeeId,
                "project_id", accessibleProjects);
    }

    @Override
    public Mono<Integer> addAccessibleProject(int employeeId, int projectId) {
        return dbTemplate.getDatabaseClient().sql(
                        "insert into employee_accessible_projects (employee_id, project_id)" +
                                " values (:employee_id, :project_id)")
                .bind("employee_id", employeeId)
                .bind("project_id", projectId)
                .fetch().rowsUpdated();
    }

    private <T> Mono<Integer> doDeleteAndInsert(String tableName,
                                                String idFieldName,
                                                int id,
                                                String valueFieldName,
                                                List<T> values) {
        var inserts = new ArrayList<Mono<Integer>>();
        for (var r : values) {
            var i = dbTemplate.getDatabaseClient().sql(
                            "insert into " + tableName + " (" + idFieldName + ", " + valueFieldName + ")" +
                                    " values (:id, :value)"
                    )
                    .bind("id", id)
                    .bind("value", r).fetch().rowsUpdated();
            inserts.add(i);
        }
        // Delete all rows
        return dbTemplate.getDatabaseClient().sql("delete from " + tableName + " where " + idFieldName + "=:id")
                .bind("id", id)
                .fetch().rowsUpdated()
                // 2. Insert new roles
                .then(values.isEmpty() ? Mono.just(0) : Mono.zip(inserts, (a) -> a.length));
    }
}
