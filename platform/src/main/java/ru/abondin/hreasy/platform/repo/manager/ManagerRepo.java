package ru.abondin.hreasy.platform.repo.manager;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.OffsetDateTime;

@Repository
public interface ManagerRepo extends ReactiveCrudRepository<ManagerEntry, Integer> {

    String defaultSelectQuery = """
            select m.*,
            e.display_name as employee_display_name
            , (e.date_of_dismissal is null or e.date_of_dismissal > :now) as employee_active
            ,CASE WHEN m.object_type='project' THEN p.name
                  WHEN m.object_type='business_account' THEN ba.name
                  WHEN m.object_type='department' THEN d.name
                  ELSE concat('UNSUPPORTED OBJECT TYPE', m.object_type)
             END as object_name
            from empl.manager m
            left join empl.employee e on m.employee=e.id
            left join ba.business_account ba on m.object_id=ba.id
            left join proj.project p on m.object_id=p.id
            left join dict.department d on m.object_id=d.id
            """;

    String AGGREGATED_MANAGERS_BY_OBJECT= """
            select m.object_id, m.object_type, jsonb_agg(jsonb_build_object(
            					'id', m.id,
            					'employeeId', e.id,
            					'employeeName', e.display_name,
            					'responsibilityType', m.responsibility_type,
            					'comment',m.comment
            					)) as managers_json
            				from empl.manager m
            				left join empl.employee e on m.employee=e.id                           
            				group by m.object_id, m.object_type
            """;

    /**
     * For every employee return array of all emails of all not fired manager:
     * 1. Get employee project
     * 2. Get project managers
     * 3. Get project's ba managers
     * 4. Get project's departments managers
     */
    String EMPLOYEE_ALL_MANAGER_EMAILS= """
            select e.id as id, array_remove(array_agg(em.email),null) as emails from empl.employee e
            	left join proj.project p on e.current_project = p.id
            	left join empl.manager m
            		on (m.object_type='project' and m.object_id=p.id)
            		or (m.object_type='business_account' and m.object_id=p.ba_id)
            		or (m.object_type='department' and m.object_id=p.department_id)
            	left join empl.employee em on m.employee=em.id
            	where
            	(em.date_of_dismissal is null or em.date_of_dismissal > :now)
            	group by e.id
            """;

    @Query(defaultSelectQuery + " order by employee_display_name asc")
    Flux<ManagerView> findDetailed(@Param("now") OffsetDateTime now);

    @Query(defaultSelectQuery + " where object_type=:objectType and object_id=:objectId order by employee_display_name asc")
    Flux<ManagerView> findByObjectDetailed(OffsetDateTime now, String objectType, int objectId);

    /**
     * Finds active managers of the employee current project, project business account, and project department
     * who have the requested permission.
     *
     * @param employeeId employee whose project hierarchy is used to resolve managers
     * @param permission required permission
     * @param now current time used to exclude dismissed managers
     * @return matching manager recipients
     */
    @Query("""
            select distinct manager_employee.id as employee_id,
                   manager_employee.email,
                   manager_employee.display_name
            from empl.employee employee
                join proj.project project on employee.current_project = project.id
                join empl.manager manager
                    on (manager.object_type = 'project' and manager.object_id = project.id)
                    or (manager.object_type = 'business_account' and manager.object_id = project.ba_id)
                    or (manager.object_type = 'department' and manager.object_id = project.department_id)
                join empl.employee manager_employee on manager.employee = manager_employee.id
                join sec.user_role user_role on user_role.employee_id = manager_employee.id
                join sec.role_perm role_perm on role_perm.role = user_role.role
            where employee.id = :employeeId
              and manager_employee.id <> employee.id
              and role_perm.permission = :permission
              and (manager_employee.date_of_dismissal is null or manager_employee.date_of_dismissal > :now)
            order by manager_employee.display_name
            """)
    Flux<ManagerRecipient> findActiveEmployeeManagersWithPermission(@Param("employeeId") int employeeId,
                                                                    @Param("permission") String permission,
                                                                    @Param("now") OffsetDateTime now);
}
