package ru.abondin.hreasy.platform.service

import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import ru.abondin.hreasy.platform.api.model.VacationDto
import ru.abondin.hreasy.platform.repo.VacationEntry
import ru.abondin.hreasy.platform.repo.VacationRepo

@Component
class VacationService(val vacationRepo: VacationRepo) {
    fun findAll(): Flux<VacationDto> = vacationRepo.findAll().map { e -> vacationToDTO(e) };
}

fun vacationToDTO(entry: VacationEntry): VacationDto {
    return VacationDto(
            entry.id,
            entry.employee,
            entry.year,
            entry.startDate,
            entry.endDate,
            entry.notes
    );
}
