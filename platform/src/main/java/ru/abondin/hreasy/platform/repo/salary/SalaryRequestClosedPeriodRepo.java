package ru.abondin.hreasy.platform.repo.salary;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import ru.abondin.hreasy.platform.repo.overtime.OvertimeClosedPeriodEntry;

public interface SalaryRequestClosedPeriodRepo extends R2dbcRepository<SalaryRequestClosedPeriodEntry, Integer> {
}
