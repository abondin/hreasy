package ru.abondin.hreasy.platform.service

import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import ru.abondin.hreasy.platform.api.employee.SimpleDictDto
import ru.abondin.hreasy.platform.config.AuthContext
import ru.abondin.hreasy.platform.repo.DictProjectEntry
import ru.abondin.hreasy.platform.repo.ProjectRepo

@Component
class DictService(val projectRepo: ProjectRepo, val dateTimeService: DateTimeService) {
    fun findProjects(auth: AuthContext, includeEnded: Boolean = false): Flux<SimpleDictDto> = projectRepo
            .findNotEnded(if (includeEnded) null else dateTimeService.now())
            .map { e -> projectToDTO(e) };
}


fun projectToDTO(e: DictProjectEntry): SimpleDictDto {
    return SimpleDictDto(e.id!!, e.name);
}
