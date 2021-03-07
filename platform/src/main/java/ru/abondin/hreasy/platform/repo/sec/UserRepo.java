package ru.abondin.hreasy.platform.repo.sec;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserRepo extends ReactiveCrudRepository<UserEntry, Integer> {
    @Query("select u.id from sec_user u where u.email=:email")
    Mono<Integer> findIdByEmail(String email);

    @Query("select u.id from sec_user u where u.employee_id=:employeeId")
    Mono<Integer> findIdByEmployeeId(int employeeId);
}
