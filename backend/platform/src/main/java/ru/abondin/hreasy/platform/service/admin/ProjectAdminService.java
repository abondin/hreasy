package ru.abondin.hreasy.platform.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.dict.DictProjectEntry;
import ru.abondin.hreasy.platform.repo.dict.DictProjectHistoryRepo;
import ru.abondin.hreasy.platform.repo.dict.DictProjectRepo;
import ru.abondin.hreasy.platform.repo.dict.ProjectWorkstreamRepo;
import ru.abondin.hreasy.platform.repo.employee.admin.SecAdminUserRolesRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.admin.dto.ProjectDto;
import ru.abondin.hreasy.platform.service.admin.dto.ProjectDtoMapper;


import static org.apache.commons.lang3.StringUtils.stripToNull;

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
    private final SecAdminUserRolesRepo adminUserRolesRepo;
    private final ProjectWorkstreamRepo workstreamRepo;

    @Transactional
    public Mono<Integer> create(AuthContext auth, ProjectDto.CreateOrUpdateProjectDto newProject) {
        log.info("Creating new project by {} : {}", auth.getUsername(), newProject);
        var now = dateTimeService.now();
        var employeeId = auth.getEmployeeInfo().getEmployeeId();
        normalize(newProject);
        if (newProject.getWorkstreamRequired()) {
            throw new BusinessError("errors.project.workstream.required");
        }
        var entry = mapper.fromDto(newProject);
        entry.setCreatedAt(now);
        entry.setCreatedBy(auth.getEmployeeInfo().getEmployeeId());
        return securityValidator.validateCreateProject(auth).flatMap(s ->
                // 1. Save project
                repo.save(entry).flatMap(savedProject -> {
                    var history = mapper.historyEntry(employeeId, now, savedProject);
                    //2. Save history
                    var projectId = savedProject.getId();
                    return historyRepo.save(history).flatMap(h ->
                            //3. Save entry
                            adminUserRolesRepo.addAccessibleProject(employeeId, projectId)
                                    .thenReturn(projectId)
                    );
                }));
    }

    @Transactional
    public Mono<Integer> update(AuthContext auth, int projectId, ProjectDto.CreateOrUpdateProjectDto projectToUpdate) {
        log.info("Updating project by {} : {}", auth.getUsername(), projectToUpdate);
        var now = dateTimeService.now();
        normalize(projectToUpdate);
        return repo.findByIdForUpdate(projectId)
                .switchIfEmpty(Mono.error(new BusinessError("errors.entity.not.found", Integer.toString(projectId))))
                .flatMap(existing -> {
                    return securityValidator.validateUpdateProject(auth, existing)
                            .then(projectToUpdate.getWorkstreamRequired()
                                    ? workstreamRepo.findActiveByProjectId(projectId).hasElements()
                                        .filter(Boolean::booleanValue)
                                        .switchIfEmpty(Mono.error(new BusinessError("errors.project.workstream.required")))
                                    : Mono.just(true))
                            .flatMap(_ -> {
                                mapper.apply(existing, projectToUpdate);
                                var history = mapper.historyEntry(auth.getEmployeeInfo().getEmployeeId(), now, existing);
                                return historyRepo.save(history).then(repo.save(existing));
                            });
                })
                .map(DictProjectEntry::getId);
    }

    public Mono<ProjectDto> findById(AuthContext auth, int projectId) {
        return repo.findFullInfoById(projectId)
                .switchIfEmpty(Mono.error(new BusinessError("errors.entity.not.found", Integer.toString(projectId))))
                .flatMap(existing ->
                        securityValidator.validateUpdateProject(auth, existing)
                                .thenReturn(mapper.fromEntry(existing))
                );
    }

    public Flux<ProjectDto> findAll(AuthContext auth) {
        var now = dateTimeService.now();
        return securityValidator.validateFindAllProject(auth).thenMany(repo.findFullInfo())
                .map(entry -> {
                    var dto = mapper.fromEntry(entry);
                    dto.setActive(entry.getEndDate() == null || entry.getEndDate().isAfter(now.toLocalDate()));
                    return dto;
                });
    }

    private void normalize(ProjectDto.CreateOrUpdateProjectDto project) {
        project.setExternalId(stripToNull(project.getExternalId()));
    }
}
