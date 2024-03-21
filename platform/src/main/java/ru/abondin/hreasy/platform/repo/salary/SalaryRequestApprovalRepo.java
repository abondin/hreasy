package ru.abondin.hreasy.platform.repo.salary;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.OffsetDateTime;

@Repository
public interface SalaryRequestApprovalRepo extends ReactiveCrudRepository<SalaryRequestApprovalEntry, Integer> {
    @Query("""
            select ap.*, e.display_name as created_by_display_name from
             sal.salary_request_approval ap
             left join empl.employee e on e.id = ap.created_by
              where request_id = :requestId and (deleted_at is null or deleted_at <= :now) order by created_at desc
            """)
    Flux<SalaryRequestApprovalView> findNotDeletedByRequestId(int requestId, OffsetDateTime now);
}

