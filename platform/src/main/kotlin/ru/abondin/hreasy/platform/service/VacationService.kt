package ru.abondin.hreasy.platform.service

import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import ru.abondin.hreasy.platform.api.vacation.VacationDto
import ru.abondin.hreasy.platform.repo.VacationRepo
import ru.abondin.hreasy.platform.repo.VacationView
import java.time.LocalDate

@Component
class VacationService(val vacationRepo: VacationRepo, val dateTimeService: DateTimeService) {
    fun findAll(filter: VacationFilter): Flux<VacationDto> = vacationRepo.findAll(
            filter.endDateSince
    ).map { e -> vacationToDTO(e) };
}

data class VacationFilter(
        val endDateSince: LocalDate = LocalDate.of(2000, 1, 1)
)

fun vacationToDTO(entry: VacationView): VacationDto {
    return VacationDto(
            entry.id,
            entry.employee,
            employeeDisplayName(entry.employeeLastname, entry.employeeFirstname, entry.employeePatronymicName),
            entry.year,
            entry.startDate,
            entry.endDate,
            entry.notes
    );
}
