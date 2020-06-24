package ru.abondin.hreasy.platform.repo.employee;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.data.relational.core.query.Criteria;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Slf4j
public class EmployeeDetailedRepoImpl implements EmployeeDetailedRepo {

    private final DatabaseClient databaseClient;

    @Override
    public Flux<EmployeeDetailedEntry> findDetailed(Criteria criteria, Sort sort) {
        var select = databaseClient
                .select()
                .from(EmployeeDetailedEntry.class)
                .orderBy(sort);
        if (criteria != null) {
            select = select.matching(criteria);
        }
        return select.fetch().all();
    }

    @Override
    public Mono<EmployeeDetailedEntry> findDetailed(int id) {
        return databaseClient
                .select()
                .from(EmployeeDetailedEntry.class)
                .matching(Criteria.where("id").is(id)).fetch().one();
    }

    @Override
    public Mono<Integer> updateCurrentProject(int employeeId, int currentProjectId) {
        return databaseClient.execute("update employee set current_project=:currentProjectId where id=:employeeId")
                .bind("employeeId", employeeId)
                .bind("currentProjectId", currentProjectId).fetch().rowsUpdated();
    }
}
