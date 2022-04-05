package ru.abondin.hreasy.platform.repo.employee.skills;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface SkillGroupRepo extends ReactiveCrudRepository<SkillGroupEntry, Integer> {

    @Query("select * from empl.skill_group where archived != true order by name asc")
    Flux<SkillGroupEntry> findNotArchived();
}
