package ru.abondin.hreasy.platform.repo.employee.skills;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

@Repository
public interface SkillRepo extends ReactiveCrudRepository<SkillEntry, Integer> {

    String findWithRatingByEmployeeIdQuery = "select s.*, g.name group_name, IsNull(r.cnt,0) ratings_count, IsNull(r.av,0) average_rating from skill s left \n" +
            "\tjoin (\n" +
            "\t\tselect skill_id skill_id, count(rating) cnt, avg(rating) av from skill_rating where deleted_at is null group by skill_id\n" +
            "\t\t) r\n" +
            "\t\ton s.id = r.skill_id\n" +
            "\tjoin skill_group g on s.group_id=g.id where (s.deleted_at is null or s.deleted_at > :now)\n" +
            "\tand s.employee_id=:employeeId";

    /**
     * @param employeeId
     * @param now
     * @return
     */
    @Query(findWithRatingByEmployeeIdQuery)
    Flux<SkillWithRatingEntry> findWithRatingByEmployeeId(Integer employeeId, OffsetDateTime now);

    /**
     * @param employeeId
     * @param now
     * @return
     */
    @Query(findWithRatingByEmployeeIdQuery + " and s.id=:skillId")
    Mono<SkillWithRatingEntry> findWithRatingByEmployeeAndSkillId(Integer employeeId, Integer skillId, OffsetDateTime now);


    @Query("select distinct group_id, name from skill where shared=1")
    Flux<SkillEntry> sharedSkills();

    @Query("select * from skill where employee_id=:employeeId and group_id=:groupId and name=:name")
    Mono<SkillEntry> findUnique(int employeeId, int groupId, String name);
}
