package ru.abondin.hreasy.platform.repo.vacation;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import ru.abondin.hreasy.platform.repo.manager.ManagerRepo;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface VacationRepo extends ReactiveCrudRepository<VacationEntry, Integer> {

    @Query("select e.display_name as employee_display_name" +
            ", e.email as employee_email" +
            ", e.current_project as employee_current_project" +
            ", e.current_project_role as employee_current_project_role" +
            ", p.name as employee_current_project_name" +
            ", v.* from  vac.vacation v " +
            " inner join empl.employee e on e.id=v.employee" +
            " left join proj.project p on e.current_project=p.id" +
            " where v.year in (:years)" +
            " and e.date_of_dismissal is null or e.date_of_dismissal > :now" +
            " order by v.end_date asc")
    Flux<VacationView> findAll(List<Integer> years, OffsetDateTime now);

    /**
     * @param employeeId
     * @param now
     * @return
     */
    @Query("select v.* from vac.vacation v " +
            "where v.employee=:employeeId and (v.start_date>=:now or v.end_date>=:now)" +
            " order by v.end_date asc")
    Flux<VacationEntry> findFuture(Integer employeeId, OffsetDateTime now);


    @Query("""
            select e.display_name as employee_display_name
            , e.email as employee_email
            , e.current_project as employee_current_project
            , p.name as employee_current_project_name
            , m.emails as managers_emails
            , v.* from  vac.vacation v 
             inner join empl.employee e on e.id=v.employee
             left join proj.project p on e.current_project=p.id
             left join
             (
             """ + ManagerRepo.EMPLOYEE_ALL_MANAGER_EMAILS +
            """
                     ) m on m.id=e.id
                     where v.year = :year
                     and (e.date_of_dismissal is null or e.date_of_dismissal > :now)
                     and v.start_date>=:from and v.start_date<=:to and v.stat in (0)
                     order by v.end_date asc
                    """)
    Flux<VacationViewWithManagers> findPlannedStartedBetween(OffsetDateTime now, int year, LocalDate from, LocalDate to);
}
