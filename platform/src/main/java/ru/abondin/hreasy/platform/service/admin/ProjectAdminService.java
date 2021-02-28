package ru.abondin.hreasy.platform.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.dict.DictProjectEntry;
import ru.abondin.hreasy.platform.repo.dict.DictProjectHistoryRepo;
import ru.abondin.hreasy.platform.repo.dict.DictProjectRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.admin.dto.ProjectDto;
import ru.abondin.hreasy.platform.service.admin.dto.ProjectDtoMapper;

/**
 * Simple CRUD for Project Dictionary
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectAdminService {
    private final DictProjectRepo repo;
    private final DictProjectHistoryRepo historyRepo;
    private final DateTimeService dateTimeService;
    private final AdminSecurityValidator securityValidator;
    private final ProjectDtoMapper mapper;

    public Mono<Integer> create(AuthContext auth, ProjectDto.CreateOrUpdateProjectDto newProject) {
        log.info("Creating new project by ? : ?", auth.getUsername(), newProject);
        var now = dateTimeService.now();
        var entry = mapper.fromDto(newProject);
        entry.setCreatedAt(now);
        entry.setCreatedBy(auth.getEmployeeInfo().getEmployeeId());
        var history = mapper.historyEntry(auth.getEmployeeInfo().getEmployeeId(), now, entry);
        return securityValidator.validateCreateProject(auth).flatMap(s ->
                historyRepo.save(history).flatMap((h) ->
                        repo.save(entry).map(DictProjectEntry::getId)));
    }

    public Mono<Integer> update(AuthContext auth, int projectId, ProjectDto.CreateOrUpdateProjectDto projectToUpdate) {
        log.info("Updating project by {} : {}", auth.getUsername(), projectToUpdate);
        var now = dateTimeService.now();
        var entry = mapper.fromDto(projectToUpdate);
        return repo.findById(projectId)
                .switchIfEmpty(Mono.error(new BusinessError("entity.not.found", Integer.toString(projectId))))
                .flatMap(existing -> {
                    entry.setId(projectId);
                    entry.setCreatedBy(existing.getCreatedBy());
                    entry.setCreatedAt(existing.getCreatedAt());
                    // TODO Person of contact is now is not editable to UI
                    // Just copy it from current value from database
                    entry.setPersonOfContact(existing.getPersonOfContact());
                    var history = mapper.historyEntry(auth.getEmployeeInfo().getEmployeeId(), now, entry);
                    return securityValidator.validateUpdateProject(auth, existing)
                            .flatMap(s -> historyRepo.save(history).flatMap((h) -> repo.save(entry)));
                })
                .map(DictProjectEntry::getId);
    }

    public Flux<ProjectDto> findAll(AuthContext auth) {
        return securityValidator.validateFindAllProject(auth).flatMapMany(s ->
                repo.findFullInfo().map(mapper::fromEntry));
    }
}
