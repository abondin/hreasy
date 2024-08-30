package ru.abondin.hreasy.platform.repo.dict;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface DictOfficeLocationRepo extends ReactiveSortingRepository<DictOfficeLocationEntry, Integer>, ReactiveCrudRepository<DictOfficeLocationEntry, Integer> {
    @Query("select * from dict.office_location o where o.archived = false order by name")
    Flux<DictOfficeLocationEntry> findNotArchived();

    @Query("select map_svg from dict.office_location where id = :officeLocationId")
    Mono<String> getMap(int officeLocationId);

    @Query("update dict.office_location set map_svg = :mapSvg where id = :officeLocationId returning id")
    Mono<Integer> setMap(int officeLocationId, String mapSvg);

}
