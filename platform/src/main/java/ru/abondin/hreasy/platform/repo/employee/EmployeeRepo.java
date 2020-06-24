package ru.abondin.hreasy.platform.repo.employee;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface EmployeeRepo extends ReactiveCrudRepository<EmployeeEntry, Integer>, EmployeeDetailedRepo {
    @Query("select id from employee where email=:email")
    Mono<Integer> findIdByEmail(String email);

//    default <S extends EmployeeEntry> Mono<S> save(S entity) {
//        return Mono.error(new UnsupportedOperationException("Save method is deprecated. Use FullEmployeeRepo.save"));
//    }
}
