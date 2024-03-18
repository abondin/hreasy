package ru.abondin.hreasy.platform.repo.salary;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.OffsetDateTime;

@Repository
public interface SalaryRequestApprovalRepo extends ReactiveCrudRepository<SalaryRequestApprovalEntry, Integer> {
    @Query("select * from sal.salary_request_approval where request = :requestId and (deleted_at is null or deleted_at <= :now) order by create_at desc")
    Flux<SalaryRequestApprovalEntry> findNotDeletedByRequestId(int requestId, OffsetDateTime now);
}

