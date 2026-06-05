package ru.abondin.hreasy.platform.repo.sec;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface SecPasswdRepo extends ReactiveCrudRepository<SecPasswdEntry, Integer> {

    @Query("select * from sec.passwd where employee=:employeeId")
    Mono<SecPasswdEntry> findByEmployee(int employeeId);
}
