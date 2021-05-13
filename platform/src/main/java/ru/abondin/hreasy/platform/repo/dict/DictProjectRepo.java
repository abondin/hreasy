package ru.abondin.hreasy.platform.repo.dict;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public interface DictProjectRepo extends ReactiveCrudRepository<DictProjectEntry, Integer> {

    @Query("select * from project p order by name")
    Flux<DictProjectEntry> findAll();

    @Query("select * from project where id in (select current_project from employee where id=:employeeId)")
    Mono<DictProjectEntry> getEmployeeCurrentProject(int employeeId);

    @Query("select * from project p where p.id in (:ids) order by name")
    Flux<DictProjectEntry> findByIds(List<Integer> ids);


    @Query("select p.*, " +
            "  d.name as department_name, s" +
            "  ba.name as ba_name " +
            "from project p" +
            " left join department d on p.department_id=d.id"+
            " left join business_account ba on p.ba_id=ba.id")
    Flux<DictProjectEntry.ProjectFullEntry> findFullInfo();
}
