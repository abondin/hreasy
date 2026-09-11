package ru.abondin.hreasy.platform.service.allocation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.allocation.ResourceAllocationCommentRepository;
import ru.abondin.hreasy.platform.repo.allocation.ResourceAllocationCommentRepository.CellKey;
import ru.abondin.hreasy.platform.repo.allocation.ResourceAllocationCommentRepository.CellSummaryView;
import ru.abondin.hreasy.platform.service.allocation.dto.ResourceAllocationAnalyticsDto;
import ru.abondin.hreasy.platform.service.allocation.dto.ResourceAllocationProjectInputDto;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/** Enforces allocation analytics scope for cell comment reads and mutations. */
@Component
@RequiredArgsConstructor
public class ResourceAllocationCommentSecurityValidator {
    private final ResourceAllocationCommentRepository repository;
    private final ResourceAllocationService allocationService;
    private final ResourceAllocationSecurityValidator allocationSecurityValidator;

    public Mono<List<CellSummaryView>> visibleComments(int year, AuthContext auth) {
        return loadScope(year, auth).map(Scope::visibleComments);
    }

    public Mono<Void> validateReadable(CellKey cell, AuthContext auth) {
        return loadScope(cell.year(), auth).flatMap(scope -> {
            if (readable(scope, cell)) {
                return Mono.empty();
            }
            return allocationService.getProjectInput(cell.year(), cell.projectId(), cell.workstreamId(), auth)
                    .filter(input -> inputRowReadable(input, cell))
                    .switchIfEmpty(Mono.error(new BusinessError(HttpStatus.NOT_FOUND,
                            "errors.entity_of_type.not.found", "ResourceAllocationCell", cellId(cell))))
                    .then();
        });
    }

    private Mono<Scope> loadScope(int year, AuthContext auth) {
        return Mono.zip(
                        allocationService.getAnalytics(year, auth),
                        repository.findYearCellSummaries(year).collectList())
                .map(data -> {
                    var analytics = data.getT1();
                    Set<Integer> visibleEmployeeIds = analytics.employees().stream()
                            .map(ResourceAllocationAnalyticsDto.EmployeeDto::id)
                            .collect(Collectors.toSet());
                    var visibleComments = data.getT2().stream()
                            .filter(comment -> visibleEmployeeIds.contains(comment.employeeId())
                                    || allocationSecurityValidator.canReadProject(auth, comment.project()))
                            .toList();
                    return new Scope(analytics, visibleComments);
                });
    }

    private boolean readable(Scope scope, CellKey cell) {
        return scope.analytics().allocations().stream().anyMatch(allocation ->
                Objects.equals(allocation.employeeId(), cell.employeeId())
                        && Objects.equals(allocation.projectId(), cell.projectId())
                        && Objects.equals(allocation.workstreamId(), cell.workstreamId()))
                || scope.visibleComments().stream().anyMatch(comment -> sameRow(comment, cell));
    }

    private boolean sameRow(CellSummaryView comment, CellKey cell) {
        return Objects.equals(comment.employeeId(), cell.employeeId())
                && Objects.equals(comment.projectId(), cell.projectId())
                && Objects.equals(comment.workstreamId(), cell.workstreamId());
    }

    private boolean inputRowReadable(ResourceAllocationProjectInputDto input, CellKey cell) {
        return Objects.equals(input.selectedProjectId(), cell.projectId())
                && Objects.equals(input.selectedWorkstreamId(), cell.workstreamId())
                && input.employees().stream().anyMatch(employee -> Objects.equals(employee.id(), cell.employeeId()));
    }

    private String cellId(CellKey cell) {
        return "%d:%d:%d:%s".formatted(cell.period(), cell.employeeId(), cell.projectId(),
                cell.workstreamId() == null ? "project" : cell.workstreamId());
    }

    private record Scope(ResourceAllocationAnalyticsDto analytics, List<CellSummaryView> visibleComments) {
    }
}
