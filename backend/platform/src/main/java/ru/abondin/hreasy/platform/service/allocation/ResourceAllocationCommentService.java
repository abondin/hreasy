package ru.abondin.hreasy.platform.service.allocation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.allocation.ResourceAllocationCommentRepository;
import ru.abondin.hreasy.platform.repo.allocation.ResourceAllocationCommentRepository.CellKey;
import ru.abondin.hreasy.platform.repo.allocation.ResourceAllocationCommentRepository.CellSummaryView;
import ru.abondin.hreasy.platform.repo.allocation.ResourceAllocationCommentRepository.CommentView;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.allocation.dto.ResourceAllocationCommentDto;
import ru.abondin.hreasy.platform.service.allocation.dto.ResourceAllocationCommentDto.CellSummaryDto;
import ru.abondin.hreasy.platform.service.allocation.dto.ResourceAllocationCommentDto.RowSummaryDto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

/** Manages comment threads using the same annual read scope as allocation analytics. */
@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceAllocationCommentService {
    private final ResourceAllocationCommentRepository repository;
    private final ResourceAllocationCommentSecurityValidator securityValidator;
    private final DateTimeService dateTimeService;

    @Transactional(readOnly = true)
    public Mono<ResourceAllocationCommentDto.SummaryDto> getSummary(int year, AuthContext auth) {
        return securityValidator.visibleComments(year, auth).map(comments -> toSummary(year, comments));
    }

    @Transactional(readOnly = true)
    public Mono<List<ResourceAllocationCommentDto>> getComments(int period, int employeeId, int projectId,
                                                                 Integer workstreamId, AuthContext auth) {
        var cell = cell(period, employeeId, projectId, workstreamId);
        return securityValidator.validateReadable(cell, auth)
                .thenMany(Flux.defer(() -> repository.findCellComments(cell)))
                .map(comment -> toDto(comment, auth.getEmployeeInfo().getEmployeeId()))
                .collectList();
    }

    @Transactional
    public Mono<ResourceAllocationCommentDto> create(ResourceAllocationCommentDto.CreateBody body,
                                                       AuthContext auth) {
        var cell = cell(body.period(), body.employeeId(), body.projectId(), body.workstreamId());
        var actorId = auth.getEmployeeInfo().getEmployeeId();
        var text = body.text().trim();
        log.info("Adding resource allocation comment for period {}, employee {}, project {} by {}",
                cell.period(), cell.employeeId(), cell.projectId(), auth.getUsername());
        return securityValidator.validateReadable(cell, auth)
                .then(Mono.defer(() -> repository.insert(cell, text, dateTimeService.now(), actorId)))
                .flatMap(repository::findById)
                .switchIfEmpty(notFound("ResourceAllocationComment", "new"))
                .map(comment -> toDto(comment, actorId));
    }

    @Transactional
    public Mono<ResourceAllocationCommentDto> update(int commentId, ResourceAllocationCommentDto.UpdateBody body,
                                                       AuthContext auth) {
        var actorId = auth.getEmployeeInfo().getEmployeeId();
        log.info("Updating resource allocation comment {} by {}", commentId, auth.getUsername());
        return findReadable(commentId, auth).flatMap(comment -> {
            if (!Objects.equals(comment.createdBy(), actorId)) {
                return Mono.error(new AccessDeniedException("Only the comment author can edit it"));
            }
            return repository.update(commentId, body.text().trim(), dateTimeService.now())
                    .flatMap(updated -> updated == 1 ? repository.findById(commentId)
                            : notFound("ResourceAllocationComment", Integer.toString(commentId)))
                    .map(updated -> toDto(updated, actorId));
        });
    }

    @Transactional
    public Mono<Void> delete(int commentId, AuthContext auth) {
        var actorId = auth.getEmployeeInfo().getEmployeeId();
        log.info("Deleting resource allocation comment {} by {}", commentId, auth.getUsername());
        return findReadable(commentId, auth).flatMap(comment -> {
            if (!Objects.equals(comment.createdBy(), actorId)) {
                return Mono.error(new AccessDeniedException("Only the comment author can delete it"));
            }
            return repository.delete(commentId).flatMap(deleted -> deleted == 1
                    ? Mono.<Void>empty()
                    : notFound("ResourceAllocationComment", Integer.toString(commentId)));
        });
    }

    private Mono<CommentView> findReadable(int commentId, AuthContext auth) {
        return repository.findById(commentId)
                .switchIfEmpty(notFound("ResourceAllocationComment", Integer.toString(commentId)))
                .flatMap(comment -> securityValidator.validateReadable(comment.cell(), auth).thenReturn(comment));
    }

    private ResourceAllocationCommentDto.SummaryDto toSummary(int year, List<CellSummaryView> comments) {
        var rows = new LinkedHashMap<RowKey, RowAccumulator>();
        for (var comment : comments) {
            var key = new RowKey(comment.employeeId(), comment.projectId(), comment.workstreamId());
            rows.computeIfAbsent(key, _ -> new RowAccumulator(comment, new ArrayList<>()))
                    .cells().add(new CellSummaryDto(comment.period(), comment.commentCount()));
        }
        return new ResourceAllocationCommentDto.SummaryDto(year, rows.values().stream()
                .map(this::toRowSummary)
                .toList());
    }

    private RowSummaryDto toRowSummary(RowAccumulator row) {
        var source = row.source();
        var project = source.project();
        return new RowSummaryDto(
                new ResourceAllocationCommentDto.EmployeeDto(
                        source.employeeId(), source.employeeName(), source.employeeEmail(),
                        source.employeeDepartmentId(), source.employeeDepartmentName(),
                        source.currentProjectId(), source.currentProjectName(), source.currentProjectRole()),
                new ResourceAllocationCommentDto.ProjectDto(
                        project.id(), project.name(), project.departmentId(), project.departmentName(),
                        project.baId(), project.baName()),
                source.workstreamId() == null ? null
                        : new ResourceAllocationCommentDto.WorkstreamDto(
                                source.workstreamId(), source.workstreamName()),
                row.cells());
    }

    private ResourceAllocationCommentDto toDto(CommentView comment, int actorId) {
        return new ResourceAllocationCommentDto(
                comment.id(), comment.text(),
                new ResourceAllocationCommentDto.AuthorDto(comment.createdBy(), comment.authorName()),
                comment.createdAt(), comment.updatedAt(), Objects.equals(comment.createdBy(), actorId));
    }

    private CellKey cell(int period, int employeeId, int projectId, Integer workstreamId) {
        var year = period / 100;
        if (year <= 0 || period % 100 < 0 || period % 100 > 11) {
            throw new BusinessError("errors.resource_allocation.invalid_period", Integer.toString(period));
        }
        return new CellKey(year, period, employeeId, projectId, workstreamId);
    }

    private <T> Mono<T> notFound(String type, String id) {
        return Mono.error(new BusinessError(HttpStatus.NOT_FOUND,
                "errors.entity_of_type.not.found", type, id));
    }

    private record RowKey(Integer employeeId, Integer projectId, Integer workstreamId) {
    }

    private record RowAccumulator(CellSummaryView source, List<CellSummaryDto> cells) {
    }
}
