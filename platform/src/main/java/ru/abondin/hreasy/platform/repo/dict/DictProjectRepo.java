package ru.abondin.hreasy.platform.repo.dict;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.repo.manager.ManagerRepo;

import java.util.List;

@Repository
public interface DictProjectRepo extends ReactiveSortingRepository<DictProjectEntry, Integer>, ReactiveCrudRepository<DictProjectEntry, Integer>  {

    String FULL_INFO_QUERY = """
            select p.*, d.name as department_name ,ba.name as ba_name
            from proj.project p
            left join dict.department d on p.department_id=d.id
            left join ba.business_account ba on p.ba_id=ba.id
            """;

    String WITH_MANAGERS_QUERY =
            "select pr.*, mgs.managers_json from ("
                    + FULL_INFO_QUERY
                    + " ) pr left join ( "
                    + ManagerRepo.AGGREGATED_MANAGERS_BY_OBJECT
                    + ") mgs on pr.id=mgs.object_id and mgs.object_type='project'";

    @Query("select * from proj.project p order by name")
    Flux<DictProjectEntry> findAll();

    @Query("select * from proj.project where id in (select current_project from empl.employee where id=:employeeId)")
    Mono<DictProjectEntry> getEmployeeCurrentProject(int employeeId);

    @Query("select * from proj.project p where p.id in (:ids) order by name")
    Flux<DictProjectEntry> findByIds(List<Integer> ids);

    @Query(FULL_INFO_QUERY +
            "where p.id=:projectId")
    Mono<DictProjectEntry.ProjectFullEntry> findFullInfoById(int projectId);

    @Query(WITH_MANAGERS_QUERY +
            "where pr.id=:projectId")
    Mono<DictProjectEntry.ProjectFullEntryWithManagers> findFullInfoWithManagersById(int projectId);


    @Query(FULL_INFO_QUERY +
            " order by p.name")
    Flux<DictProjectEntry.ProjectFullEntry> findFullInfo();
}
