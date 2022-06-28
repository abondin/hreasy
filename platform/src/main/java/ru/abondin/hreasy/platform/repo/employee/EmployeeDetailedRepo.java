package ru.abondin.hreasy.platform.repo.employee;

import org.springframework.data.domain.Sort;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EmployeeDetailedRepo {
    Flux<EmployeeDetailedEntry> findDetailed(Criteria criteria, @Nullable Sort sort);

    Mono<EmployeeDetailedEntry> findDetailed(int id);

}
