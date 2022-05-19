package ru.abondin.hreasy.platform.repo.dict;

import io.micrometer.core.instrument.config.MeterFilter;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public interface DictProjectRepo extends ReactiveSortingRepository<DictProjectEntry, Integer> {

    @Query("select * from proj.project p order by name")
    Flux<DictProjectEntry> findAll();

    @Query("select * from proj.project where id in (select current_project from empl.employee where id=:employeeId)")
    Mono<DictProjectEntry> getEmployeeCurrentProject(int employeeId);

    @Query("select * from proj.project p where p.id in (:ids) order by name")
    Flux<DictProjectEntry> findByIds(List<Integer> ids);


    @Query("select p.*, " +
            "  d.name as department_name" +
            "  ,ba.name as ba_name " +
            "from proj.project p" +
            " left join dict.department d on p.department_id=d.id"+
            " left join ba.business_account ba on p.ba_id=ba.id" +
            " order by p.name")
    Flux<DictProjectEntry.ProjectFullEntry> findFullInfo();
}
