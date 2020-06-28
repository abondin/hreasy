package ru.abondin.hreasy.platform.repo.vacation;

import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

@RequiredArgsConstructor
@Service
public class VacationRepoImpl implements VacationRepo {
    private final DatabaseClient databaseClient;

    @Override
    public Flux<VacationView> findAll(LocalDate endDateSince) {
        var sql = "select v.*, e.firstname as employee_firstname, e.lastname as employee_lastname, e.patronymic_name as employee_patronymic_name" +
                " , e.current_project as employee_current_project"
                + " from vacation v" +
                " inner join employee e on e.id=v.employee"
                + " where v.end_date>=:endDateSince"
                + " order by v.end_date asc";
        var select = databaseClient
                .execute(sql)
                .bind("endDateSince", endDateSince)
                .as(VacationView.class);
        return select.fetch().all();
    }
}
