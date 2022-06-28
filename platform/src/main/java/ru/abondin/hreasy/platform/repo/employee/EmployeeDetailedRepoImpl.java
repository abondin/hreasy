package ru.abondin.hreasy.platform.repo.employee;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

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
    public Flux<String> uniqueCurrentProjectRoles(OffsetDateTime now) {
        return dbTemplate.getDatabaseClient().sql("select distinct e.current_project_role from empl.employee e" +
                        " where e.current_project_role is not null" +
                        " and (e.date_of_dismissal is null or e.date_of_dismissal > :now)" +
                        " order by e.current_project_role")
                .bind("now", now)
                .map(r -> r.get(0, String.class)).all();
    }
}
