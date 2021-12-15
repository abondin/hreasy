package ru.abondin.hreasy.platform.repo.sec;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PermissionRepo extends ReactiveCrudRepository<PermissionEntry, Integer> {

    @Query("select p.* from sec.role_perm p" +
            " join sec.user_role r on p.role=r.role" +
            " where r.employee_id=:employeeId")
    Flux<PermissionEntry> findByUserId(int employeeId);
}
