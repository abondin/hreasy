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
import ru.abondin.hreasy.platform.repo.dict.ProjectWorkstreamEntry;
import ru.abondin.hreasy.platform.repo.dict.ProjectWorkstreamRepo;
import ru.abondin.hreasy.platform.repo.employee.admin.SecAdminUserRolesRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.admin.dto.ProjectDto;
import ru.abondin.hreasy.platform.service.admin.dto.ProjectDtoMapper;
import ru.abondin.hreasy.platform.service.dto.ProjectWorkstreamDto;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

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
                                    .then(syncWorkstreams(projectId, newProject.getWorkstreams(), employeeId, now))
                                    .thenReturn(projectId)
                    );
                }));
    }

    @Transactional
    public Mono<Integer> update(AuthContext auth, int projectId, ProjectDto.CreateOrUpdateProjectDto projectToUpdate) {
        log.info("Updating project by {} : {}", auth.getUsername(), projectToUpdate);
        var now = dateTimeService.now();
        normalize(projectToUpdate);
        return repo.findById(projectId)
                .switchIfEmpty(Mono.error(new BusinessError("errors.entity.not.found", Integer.toString(projectId))))
                .flatMap(existing -> {
                    mapper.apply(existing, projectToUpdate);
                    var history = mapper.historyEntry(auth.getEmployeeInfo().getEmployeeId(), now, existing);
                    return securityValidator.validateUpdateProject(auth, existing)
                            .flatMap(s -> historyRepo.save(history)
                                    .then(repo.save(existing))
                                    .then(syncWorkstreams(projectId, projectToUpdate.getWorkstreams(),
                                            auth.getEmployeeInfo().getEmployeeId(), now))
                                    .thenReturn(existing));
                })
                .map(DictProjectEntry::getId);
    }

    public Mono<ProjectDto> findById(AuthContext auth, int projectId) {
        return repo.findFullInfoById(projectId)
                .switchIfEmpty(Mono.error(new BusinessError("errors.entity.not.found", Integer.toString(projectId))))
                .flatMap(existing ->
                        securityValidator.validateUpdateProject(auth, existing)
                                .then(workstreamRepo.findActiveByProjectId(projectId).map(this::toDto).collectList())
                                .map(workstreams -> withWorkstreams(mapper.fromEntry(existing), workstreams))
                );
    }

    public Flux<ProjectDto> findAll(AuthContext auth) {
        var now = dateTimeService.now();
        return securityValidator.validateFindAllProject(auth).thenMany(Mono.zip(
                        repo.findFullInfo().collectList(),
                        workstreamRepo.findActive().collectMultimap(ProjectWorkstreamEntry::getProjectId)))
                .flatMap(data -> Flux.fromIterable(data.getT1()).map(entry -> {
                    var dto = mapper.fromEntry(entry);
                    dto.setActive(entry.getEndDate() == null || entry.getEndDate().isAfter(now.toLocalDate()));
                    return withWorkstreams(dto, data.getT2().getOrDefault(entry.getId(), List.of()).stream()
                            .map(this::toDto).toList());
                }));
    }

    private Mono<Void> syncWorkstreams(int projectId, List<ProjectWorkstreamDto> requested,
                                       int employeeId, OffsetDateTime now) {
        if (requested == null) {
            return Mono.empty();
        }
        var ids = new HashSet<Integer>();
        var externalIds = new HashSet<String>();
        for (var item : requested) {
            if (item == null || item.displayName() == null || item.displayName().trim().isEmpty()
                    || item.displayName().trim().length() > 255 || (item.id() != null && !ids.add(item.id()))
                    || (item.externalId() != null && !item.externalId().isBlank()
                    && (!externalIds.add(item.externalId().trim()) || item.externalId().trim().length() > 255))) {
                return Mono.error(new BusinessError("errors.project.workstream.invalid"));
            }
        }
        return workstreamRepo.findActiveByProjectId(projectId).collectList().flatMap(existing -> {
            var byId = new HashMap<Integer, ProjectWorkstreamEntry>();
            existing.forEach(item -> byId.put(item.getId(), item));
            var entries = requested.stream().map(item -> {
                var entry = item.id() == null ? new ProjectWorkstreamEntry() : byId.remove(item.id());
                if (entry == null) {
                    throw new BusinessError("errors.project.workstream.invalid");
                }
                if (entry.getId() == null) {
                    entry.setProjectId(projectId);
                    entry.setCreatedAt(now);
                    entry.setCreatedBy(employeeId);
                } else if (!Objects.equals(entry.getExternalId(), normalize(item.externalId()))
                        || !Objects.equals(entry.getDisplayName(), item.displayName().trim())
                        || !Objects.equals(entry.getDescription(), normalize(item.description()))) {
                    entry.setUpdatedAt(now);
                    entry.setUpdatedBy(employeeId);
                }
                entry.setExternalId(normalize(item.externalId()));
                entry.setDisplayName(item.displayName().trim());
                entry.setDescription(normalize(item.description()));
                return entry;
            }).toList();
            byId.values().forEach(item -> {
                item.setDeletedAt(now);
                item.setDeletedBy(employeeId);
            });
            return workstreamRepo.saveAll(Flux.concat(Flux.fromIterable(byId.values()), Flux.fromIterable(entries)))
                    .then();
        });
    }

    private void normalize(ProjectDto.CreateOrUpdateProjectDto project) {
        project.setExternalId(normalize(project.getExternalId()));
        if (project.getExternalId() != null && project.getExternalId().length() > 255) {
            throw new BusinessError("errors.project.workstream.invalid");
        }
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private ProjectWorkstreamDto toDto(ProjectWorkstreamEntry entry) {
        return new ProjectWorkstreamDto(entry.getId(), entry.getExternalId(),
                entry.getDisplayName(), entry.getDescription());
    }

    private ProjectDto withWorkstreams(ProjectDto project, List<ProjectWorkstreamDto> workstreams) {
        project.setWorkstreams(workstreams);
        return project;
    }
}
