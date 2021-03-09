package ru.abondin.hreasy.platform.repo.employee.skills;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

@Repository
public interface SkillRepo extends ReactiveCrudRepository<SkillEntry, Integer> {
    /**
     * @param employeeId
     * @param now
     * @return
     */
    @Query("select s.*, g.name group_name from skill s" +
            " join skill_group g on s.group_id=g.id" +
            " where" +
            " s.deleted_at is null or s.deleted_at > :now" +
            " and s.employee_id=:employeeId")
    Flux<SkillWithRatingEntry> findWithRatingByEmployeeId(Integer employeeId, OffsetDateTime now);

    @Query("select distinct group_id, name from skill")
    Flux<SkillEntry> sharedSkills();

    @Query("select * from skill where employee_id=:employeeId and group_id=:groupId and name=:name")
    Mono<SkillEntry> findUnique(int employeeId, int groupId, String name);
}
