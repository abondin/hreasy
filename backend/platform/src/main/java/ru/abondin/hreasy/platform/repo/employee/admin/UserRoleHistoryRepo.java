package ru.abondin.hreasy.platform.repo.employee.admin;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRoleHistoryRepo extends ReactiveCrudRepository<UserRoleHistoryEntry, Integer> {
}
