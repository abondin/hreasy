package ru.abondin.hreasy.platform.repo.dict;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DictWorkingDayCalendarRepo extends ReactiveCrudRepository<DictWorkingDaysCalendarEntry, DictWorkingDaysCalendarEntry.WorkingDaysCalendarEntryPk> {
}
