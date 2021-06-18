package ru.abondin.hreasy.platform.repo.employee.admin;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface EmployeeWithAllDetailsRepo extends ReactiveCrudRepository<EmployeeWithAllDetailsEntry, Integer> {

    @Query("select * from v_employee_detailed where email=:email")
    Mono<EmployeeWithAllDetailsEntry> findByEmail(String email);
}
