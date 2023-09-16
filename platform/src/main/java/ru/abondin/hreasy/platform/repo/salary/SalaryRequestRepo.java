package ru.abondin.hreasy.platform.repo.salary;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SalaryRequestRepo extends ReactiveCrudRepository<SalaryRequestEntry, Integer> {
}

