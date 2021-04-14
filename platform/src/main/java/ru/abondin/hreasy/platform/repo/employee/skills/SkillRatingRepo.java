package ru.abondin.hreasy.platform.repo.employee.skills;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

@Repository
public interface SkillRatingRepo extends ReactiveCrudRepository<SkillRatingEntry, Integer> {

    @Query("select * from skill_rating where created_by=:employeeId and skill_id=:skillId")
    Mono<SkillRatingEntry> findByCreatedByAndSkillId(Integer employeeId, Integer skillId);

    @Query("update skill_rating set deleted_by=:employeeId, deleted_at=:now where skill_id=:skillId")
    Mono<Integer> markAllAsDeleted(int skillId, int employeeId, OffsetDateTime now);
}
