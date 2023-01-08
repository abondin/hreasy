package ru.abondin.hreasy.platform.repo.employee.admin;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

@Repository
public interface EmployeeWithAllDetailsRepo extends ReactiveCrudRepository<EmployeeWithAllDetailsEntry, Integer> {

    @Query("select * from empl.v_employee_detailed where email=:email")
    Mono<EmployeeWithAllDetailsEntry> findByEmailsInLowerCase(String email);

    @Query("select * from empl.v_employee_detailed where lower(email) in (:emails)")
    Flux<EmployeeWithAllDetailsEntry> findByEmailsInLowerCase(Set<String> emails);

    @Query("select * from empl.v_employee_detailed order by display_name")
    Flux<EmployeeWithAllDetailsWithBaView> findAllDetailed();

    @Query("select * from empl.v_employee_detailed where id=:id order by display_name")
    Mono<EmployeeWithAllDetailsWithBaView> findDetailedById(int id);

}
