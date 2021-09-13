package ru.abondin.hreasy.platform.repo.employee;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Slf4j
public class EmployeeDetailedRepoImpl implements EmployeeDetailedRepo {

    private final R2dbcEntityTemplate dbTemplate;

    @Override
    public Flux<EmployeeDetailedEntry> findDetailed(Criteria criteria, Sort sort) {
        var query = criteria == null ? Query.empty() : Query.query(criteria);
        query = query.sort(sort);
        return dbTemplate
                .select(query, EmployeeDetailedEntry.class);
    }

    @Override
    public Mono<EmployeeDetailedEntry> findDetailed(int id) {
        return dbTemplate
                .selectOne(
                        Query.query(Criteria.where("id").is(id)),
                        EmployeeDetailedEntry.class);
    }

    @Override
    public Mono<Integer> updateCurrentProject(int employeeId, @Nullable Integer currentProjectId) {
        var sql = dbTemplate
                .getDatabaseClient().sql("update employee set current_project=:currentProjectId where id=:employeeId")
                .bind("employeeId", employeeId);
        if (currentProjectId == null) {
            sql = sql.bindNull("currentProjectId", Integer.class);
        } else {
            sql = sql.bind("currentProjectId", currentProjectId);
        }
        return sql.fetch().rowsUpdated();
    }
}
