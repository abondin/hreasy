package ru.abondin.hreasy.platform.repo.employee.admin;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeHistoryRepo extends ReactiveCrudRepository<EmployeeHistoryEntry, Integer> {
}
