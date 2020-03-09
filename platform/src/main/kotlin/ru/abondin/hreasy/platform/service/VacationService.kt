package ru.abondin.hreasy.platform.service

import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import ru.abondin.hreasy.platform.api.model.VacationDto
import ru.abondin.hreasy.platform.repo.VacationRepo
import ru.abondin.hreasy.platform.repo.VacationView
import java.time.LocalDate

@Component
class VacationService(val vacationRepo: VacationRepo, val dateTimeService: DateTimeService) {
    fun findAll(startsFrom: LocalDate?): Flux<VacationDto> = vacationRepo.findAll(
            startsFrom ?: LocalDate.of(2000, 1, 1)
    ).map { e -> vacationToDTO(e) };
}

fun vacationToDTO(entry: VacationView): VacationDto {
    return VacationDto(
            entry.id,
            entry.employee,
            employeeDisplayName(entry.employeeLastname, entry.employeeFirstname, entry.employeePatronymicName),
            entry.year,
            entry.start_date,
            entry.end_date,
            entry.notes
    );
}
