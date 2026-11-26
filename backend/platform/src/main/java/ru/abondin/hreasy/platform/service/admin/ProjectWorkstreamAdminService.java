package ru.abondin.hreasy.platform.service.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.dict.DictProjectEntry;
import ru.abondin.hreasy.platform.repo.dict.DictProjectRepo;
import ru.abondin.hreasy.platform.repo.dict.ProjectWorkstreamEntry;
import ru.abondin.hreasy.platform.repo.dict.ProjectWorkstreamRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.HistoryDomainService;
import ru.abondin.hreasy.platform.service.admin.dto.ProjectWorkstreamBody;
import ru.abondin.hreasy.platform.service.dto.ProjectWorkstreamDto;

import java.util.Objects;

import static org.apache.commons.lang3.StringUtils.stripToNull;

/** Edits individual project workstreams with project-scoped authorization and audit history. */
@Service
@RequiredArgsConstructor
public class ProjectWorkstreamAdminService {
    private final DictProjectRepo projects;
    private final ProjectWorkstreamRepo workstreams;
    private final AdminSecurityValidator security;
    private final HistoryDomainService history;
    private final DateTimeService clock;

    public Flux<ProjectWorkstreamDto> findAll(AuthContext auth, int projectId) {
        return authorize(auth, projectId, false)
                .thenMany(workstreams.findActiveByProjectId(projectId))
                .map(entry -> new ProjectWorkstreamDto(entry.getId(), entry.getExternalId(),
                        entry.getDisplayName(), entry.getDescription(), entry.getArchivedAt()));
    }

    @Transactional
    public Mono<Integer> create(AuthContext auth, int projectId, ProjectWorkstreamBody body) {
        return authorize(auth, projectId, true).flatMap(_ -> {
            var entry = new ProjectWorkstreamEntry();
            entry.setProjectId(projectId);
            return save(auth, entry, body);
        });
    }

    @Transactional
    public Mono<Integer> update(AuthContext auth, int projectId, int workstreamId, ProjectWorkstreamBody body) {
        return authorize(auth, projectId, true).then(findWorkstream(projectId, workstreamId))
                .flatMap(entry -> save(auth, entry, body));
    }

    @Transactional
    public Mono<Integer> delete(AuthContext auth, int projectId, int workstreamId) {
        return authorize(auth, projectId, true).flatMap(project -> findWorkstream(projectId, workstreamId)
                .flatMap(entry -> (project.isWorkstreamRequired()
                        ? workstreams.findActiveByProjectId(projectId).any(other -> !other.getId().equals(workstreamId))
                            .filter(Boolean::booleanValue)
                            .switchIfEmpty(Mono.error(new BusinessError("errors.project.workstream.required")))
                        : Mono.just(true))
                        .flatMap(_ -> {
                            entry.setDeletedAt(clock.now());
                            entry.setDeletedBy(auth.getEmployeeInfo().getEmployeeId());
                            return persist(auth, entry);
                        })));
    }

    private Mono<DictProjectEntry> authorize(AuthContext auth, int projectId, boolean lock) {
        return (lock ? projects.findByIdForUpdate(projectId) : projects.findById(projectId))
                .switchIfEmpty(Mono.error(new BusinessError("errors.entity.not.found", Integer.toString(projectId))))
                .flatMap(project -> security.validateUpdateProject(auth, project).thenReturn(project));
    }

    private Mono<ProjectWorkstreamEntry> findWorkstream(int projectId, int id) {
        return workstreams.findById(id)
                .filter(entry -> entry.getDeletedAt() == null && Objects.equals(entry.getProjectId(), projectId))
                .switchIfEmpty(Mono.error(new BusinessError("errors.project.workstream.invalid")));
    }

    private Mono<Integer> save(AuthContext auth, ProjectWorkstreamEntry entry, ProjectWorkstreamBody body) {
        var name = stripToNull(body.displayName());
        if (name == null) {
            return Mono.error(new BusinessError("errors.project.workstream.invalid"));
        }
        var externalId = stripToNull(body.externalId());
        var description = stripToNull(body.description());
        boolean archiveChanged = body.archived() != (entry.getArchivedAt() != null);
        if (entry.getId() != null && Objects.equals(entry.getDisplayName(), name)
                && Objects.equals(entry.getExternalId(), externalId)
                && Objects.equals(entry.getDescription(), description) && !archiveChanged) {
            return Mono.just(entry.getId());
        }
        var now = clock.now();
        var employeeId = auth.getEmployeeInfo().getEmployeeId();
        if (entry.getId() == null) {
            entry.setCreatedAt(now);
            entry.setCreatedBy(employeeId);
        } else {
            entry.setUpdatedAt(now);
            entry.setUpdatedBy(employeeId);
        }
        if (archiveChanged) {
            entry.setArchivedAt(body.archived() ? now : null);
            entry.setArchivedBy(body.archived() ? employeeId : null);
        }
        entry.setExternalId(externalId);
        entry.setDisplayName(name);
        entry.setDescription(description);
        return persist(auth, entry);
    }

    private Mono<Integer> persist(AuthContext auth, ProjectWorkstreamEntry entry) {
        return workstreams.save(entry).flatMap(saved -> history.persistHistory(saved.getId(),
                HistoryDomainService.HistoryEntityType.PROJECT_WORKSTREAM, saved,
                clock.now(), auth.getEmployeeInfo().getEmployeeId()).thenReturn(saved.getId()));
    }
}
