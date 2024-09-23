package ru.abondin.hreasy.platform.repo.dict;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface DictOfficeWorkplaceRepo extends ReactiveSortingRepository<DictOfficeWorkplaceEntry, Integer>, ReactiveCrudRepository<DictOfficeWorkplaceEntry, Integer> {
    @Query("select * from dict.office_workplace o where o.archived = false order by name")
    Flux<DictOfficeWorkplaceEntry> findNotArchived();

    @Query("select w.*, l.name office_location_name, o.id office_id, o.name office_name " +
            "from dict.office_workplace w " +
            "left join dict.office_location l on w.office_location_id = l.id " +
            "left join dict.office o on l.office_id = o.id order by o.name, l.name, w.name")
    Flux<DictOfficeWorkplaceView> findAllView();

    @Query("select * from dict.office_workplace where office_location_id = :officeLocationId and id = :workplaceId")
    Mono<DictOfficeWorkplaceEntry> findByOfficeLocationAndId(int officeLocationId, int workplaceId);
}
