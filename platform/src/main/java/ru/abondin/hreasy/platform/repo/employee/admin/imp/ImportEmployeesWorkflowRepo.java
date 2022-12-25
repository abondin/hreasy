package ru.abondin.hreasy.platform.repo.employee.admin.imp;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ImportEmployeesWorkflowRepo extends ReactiveCrudRepository<ImportEmployeesWorkflowEntry, Integer> {
    /**
     * Get not c
     * @param employeeId
     * @return
     */
    @Query("select * from empl.import_workflow where state>=0 and created_by=:employeeId limit 1")
    Mono<ImportEmployeesWorkflowEntry> get(@Param("employeeId") int employeeId);
}
