package ru.abondin.hreasy.platform.repo.employee;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.repo.employee.admin.UserSecurityInfoEntry;

@Repository
public interface EmployeeRepo extends ReactiveCrudRepository<EmployeeEntry, Integer>, EmployeeDetailedRepo {
    @Query("select id from empl.employee where trim(email) ilike (:email)")
    Mono<EmployeeShortInfoEntry> findIdByEmail(String email);

    @Query("select department_id from sec.employee_accessible_departments where employee_id=:employeeId")
    Flux<Integer> findAccessibleDepartments(int employeeId);

    @Query("select ba_id from sec.employee_accessible_bas where employee_id=:employeeId")
    Flux<Integer> findAccessibleBas(int employeeId);

    @Query("select project_id from sec.employee_accessible_projects where employee_id=:employeeId")
    Flux<Integer> findAccessibleProjects(int employeeId);

    @Query("select e.id as id, e.lastname, e.firstname, e.patronymic_name,\n" +
            "e.current_project current_project, e.department department,\n" +
            "e.date_of_dismissal,\n" +
            "pr.accessible_projects as accessible_projects,\n" +
            "deps.accessible_departments as accessible_departments,\n" +
            "bas.accessible_bas as accessible_bas,\n" +
            "r.roles\n" +
            "from empl.employee e\n" +
            "\tleft join\n" +
            "\t\t(select p.employee_id, array_agg(p.project_id) accessible_projects from sec.employee_accessible_projects p group by p.employee_id) pr\n" +
            "\t\ton e.id=pr.employee_id\n" +
            "\tleft join\n" +
            "\t\t(select d.employee_id, array_agg(d.department_id) accessible_departments from sec.employee_accessible_departments d group by d.employee_id) deps\n" +
            "\t\ton e.id=deps.employee_id\n" +
            "\tleft join \n" +
            "\t\t(select r.employee_id, array_agg(r.role) roles from sec.user_role r group by r.employee_id) r\n" +
            "\t\ton e.id=r.employee_id\n" +
            "\tleft join\n" +
            "\t\t(select b.employee_id, array_agg(b.ba_id) accessible_bas from sec.employee_accessible_bas b group by b.employee_id) bas\n" +
            "\t\ton e.id=bas.employee_id")
    Flux<UserSecurityInfoEntry> findWithSecurityInfo();

//    default <S extends EmployeeEntry> Mono<S> save(S entity) {
//        return Mono.error(new UnsupportedOperationException("Save method is deprecated. Use FullEmployeeRepo.save"));
//    }

    @Data
    class EmployeeShortInfoEntry {
        @Id
        private Integer id;
        @Column("department")
        private Integer departmentId;
        @Column("current_project")
        private Integer currentProjectId;

    }
}
