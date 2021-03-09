package ru.abondin.hreasy.platform.repo.employee.skills;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SkillRatingRepo extends ReactiveCrudRepository<SkillRatingEntry, Integer> {
}
