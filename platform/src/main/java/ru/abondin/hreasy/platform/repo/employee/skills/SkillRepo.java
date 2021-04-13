package ru.abondin.hreasy.platform.repo.employee.skills;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface SkillRepo extends ReactiveCrudRepository<SkillEntry, Integer> {

    String findWithRatingByEmployeeIdQuery = "select s.*, g.name group_name, r.str ratings from skill s left" +
            " join (" +
            " select s.employee_id, s.id skill_id , s.name, STRING_AGG(CONCAT_WS(',', r.created_by, r.rating), '/') str from skill_rating r join skill s on s.id=r.skill_id and r.deleted_at is null" +
            "             group by s.employee_id, s.id, s.name" +
            " ) r" +
            " on s.id = r.skill_id" +
            " join skill_group g on s.group_id=g.id where (s.deleted_at is null)" +
            " and s.employee_id=:employeeId";

    /**
     * @param employeeId
     * @return
     */
    @Query(findWithRatingByEmployeeIdQuery)
    Flux<SkillWithRatingEntry> findWithRatingByEmployeeId(Integer employeeId);

    /**
     * @param employeeId
     * @return
     */
    @Query(findWithRatingByEmployeeIdQuery + " and s.id=:skillId")
    Mono<SkillWithRatingEntry> findWithRatingByEmployeeAndSkillId(Integer employeeId, Integer skillId);


    @Query("select distinct group_id, name from skill where shared=1")
    Flux<SkillEntry> sharedSkills();

    @Query("select * from skill where employee_id=:employeeId and group_id=:groupId and name=:name")
    Mono<SkillEntry> findUnique(int employeeId, int groupId, String name);
}
