package ru.abondin.hreasy.platform.repo.employee;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.repo.employee.admin.UserSecurityInfoEntry;

import java.time.OffsetDateTime;

@Repository
public interface EmployeeRepo extends ReactiveCrudRepository<EmployeeEntry, Integer>, EmployeeDetailedRepo {
    @Query("select id, email from empl.employee where trim(telegram) ilike (:telegram)")
    Mono<EmployeeTelegramBinding> findEmailByTelegramAccount(String telegram);

    @Query("""
                select id from empl.employee where trim(email) ilike (:email)
                and (date_of_dismissal is null or date_of_dismissal > :now)
            """)
    Mono<EmployeeShortInfoEntry> findNotDismissedIdByEmail(String email, OffsetDateTime now);

    @Query("select department_id from sec.employee_accessible_departments where employee_id=:employeeId")
    Flux<Integer> findAccessibleDepartments(int employeeId);

    @Query("select ba_id from sec.employee_accessible_bas where employee_id=:employeeId")
    Flux<Integer> findAccessibleBas(int employeeId);

    @Query("select project_id from sec.employee_accessible_projects where employee_id=:employeeId")
    Flux<Integer> findAccessibleProjects(int employeeId);

    @Query("""
            select e.id as id, e.display_name,
            e.current_project current_project, e.department department,
            e.date_of_dismissal,
            pr.accessible_projects as accessible_projects,
            deps.accessible_departments as accessible_departments,
            bas.accessible_bas as accessible_bas,
            r.roles
            from empl.employee e
                left join
                    (select p.employee_id, array_agg(p.project_id) accessible_projects from sec.employee_accessible_projects p group by p.employee_id) pr
                on e.id=pr.employee_id
                left join
                    (select d.employee_id, array_agg(d.department_id) accessible_departments from sec.employee_accessible_departments d group by d.employee_id) deps
                on e.id=deps.employee_id
                left join 
                    (select r.employee_id, array_agg(r.role) roles from sec.user_role r group by r.employee_id) r
                on e.id=r.employee_id
                left join
                    (select b.employee_id, array_agg(b.ba_id) accessible_bas from sec.employee_accessible_bas b group by b.employee_id) bas
                on e.id=bas.employee_id
            """)
    Flux<UserSecurityInfoEntry> findWithSecurityInfo();


    @Data
    class EmployeeShortInfoEntry {
        @Id
        private Integer id;
        @Column("department")
        private Integer departmentId;
        @Column("current_project")
        private Integer currentProjectId;

    }

    @Data
    class EmployeeTelegramBinding {
        @Id
        private Integer id;
        @NotNull
        private String email;
    }
}
