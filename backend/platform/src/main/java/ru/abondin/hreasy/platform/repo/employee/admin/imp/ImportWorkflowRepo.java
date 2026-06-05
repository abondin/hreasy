package ru.abondin.hreasy.platform.repo.employee.admin.imp;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ImportWorkflowRepo extends ReactiveCrudRepository<ImportWorkflowEntry, Integer> {
    /**
     * Get not completed import workflow
     *
     * @param employeeId - logged in employee who want to start or continue import
     * @param wfType
     * @return
     */
    @Query("select * from empl.import_workflow where state in (0,1,2) and " +
            "created_by=:employeeId and wf_type=:wfType order by created_at desc limit 1")
    Mono<ImportWorkflowEntry> get(@Param("employeeId") int employeeId, @Param("wfType") short wfType);
}
