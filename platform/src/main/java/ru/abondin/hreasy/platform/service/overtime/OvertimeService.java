package ru.abondin.hreasy.platform.service.overtime;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.overtime.*;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.overtime.dto.*;

import java.util.ArrayList;
import java.util.Arrays;


@Service
@RequiredArgsConstructor
@Slf4j
public class OvertimeService {
    private final OvertimeReportRepo reportRepo;
    private final OvertimeItemRepo itemRepo;
    private final OvertimeItemViewRepo itemViewRepo;
    private final OvertimeApprovalDecisionRepo approvalRepo;
    private final OvertimeMapper mapper;
    private final DateTimeService dateTimeService;
    private final OvertimeClosedPeriodRepo closedPeriodRepo;

    private final OvertimeSecurityValidator securityValidator;

    /**
     * Get report or empty stub to create new one
     *
     * @param employeeId
     * @param periodId
     * @param auth
     * @return
     */
    public Mono<OvertimeReportDto> getOrStub(int employeeId, int periodId, AuthContext auth) {
        return securityValidator.validateViewOvertime(auth, employeeId)
                .then(get(employeeId, periodId).defaultIfEmpty(stub(employeeId, periodId)));
    }

    /**
     * Add item to the report.
     * Create report entry if required
     *
     * @return
     */
    @Transactional
    public Mono<OvertimeReportDto> addItem(int employeeId, int periodId, NewOvertimeItemDto newItem, AuthContext auth) {
        // 0. Validate auth
        return securityValidator.validateEditOvertimeItem(auth, employeeId)
                .then(validatePeriodNotClosed(periodId))
                .then(
                // 1. Get report
                get(employeeId, periodId)
                        // 2. Create entry if not exists
                        .switchIfEmpty(reportRepo.save(stubEntry(employeeId, periodId))
                                .map(r -> mapper.reportToDto(r)))
                        // 3. Create item
                        .flatMap(report -> {
                            var itemEntry = mapper.itemToEntry(newItem);
                            var now = dateTimeService.now();
                            itemEntry.setCreatedAt(now);
                            itemEntry.setCreatedEmployeeId(auth.getEmployeeInfo().getEmployeeId());
                            itemEntry.setReportId(report.getId());
                            return itemRepo.save(itemEntry)
                                    .map(persistedItem -> mapper.itemToDto(persistedItem))
                                    .map(item -> {
                                        report.getItems().add(item);
                                        return report;
                                    });
                        }));
    }


    /**
     * Delete report item
     *
     * @return
     */
    @Transactional
    public Mono<OvertimeReportDto> deleteItem(int employeeId, int periodId, int itemId, AuthContext auth) {
        // 0. Validate auth
        return securityValidator.validateEditOvertimeItem(auth, employeeId)
                .then(validatePeriodNotClosed(periodId)).then(
                // 1. Get item
                itemRepo.findById(itemId)
                        .flatMap(item -> {
                            var now = dateTimeService.now();
                            item.setDeletedAt(now);
                            item.setDeletedEmployeeId(auth.getEmployeeInfo().getEmployeeId());
                            return itemRepo.save(item);
                        })
                        .switchIfEmpty(Mono.error(new BusinessError("errors.entity.not.found", Integer.toString(itemId))))
                        .then(get(employeeId, periodId)));
    }

    public Flux<OvertimeEmployeeSummary> getSummary(int period, AuthContext auth) {
        // Validate auth
        return securityValidator.validateViewOvertimeSummary(auth).thenMany(
                // Find all reports
                reportRepo.summary(period).map(mapper::summaryFromEntry)
                        // For each report find all items grouped by project and employee
                        .flatMap(summary -> itemViewRepo.gropedByProjectAndDate(summary.getReportId())
                                .map(mapper::viewToDto)
                                .collectList()
                                .map(items -> {
                                    summary.setItems(items);
                                    return summary;
                                })));
    }


    /**
     * Approve or decline overtime report
     *
     * @param employeeId
     * @param periodId
     * @param decision
     * @param previousApprovalId - automatically cancel previous approval decision of loggedin user
     * @param comment
     * @param auth
     * @return
     */
    @Transactional
    public Mono<OvertimeReportDto> approveReport(int employeeId, int periodId,
                                                 OvertimeApprovalDecisionDto.ApprovalDecision decision,
                                                 Integer previousApprovalId,
                                                 @Nullable String comment, AuthContext auth) {
        log.debug("Approve overtime report for employee: {}, period: {}, decision: {} by {}",
                employeeId, periodId, decision, auth.getUsername());
        // 1. Validate security
        return securityValidator.validateApproveOvertime(auth, employeeId)
                .then(validatePeriodNotClosed(periodId)).then(
                // 2. Get Overtime Report
                get(employeeId, periodId).flatMap(report ->
                        // 3. Cancel previous approval decision if required
                        cancelPreviousApproval(previousApprovalId, auth).then(
                                // 4. Check that we don't have not canceled approval from given manager
                                approvalRepo.existsNotCanceled(report.getId(), auth.getEmployeeInfo().getEmployeeId()).flatMap(exists -> {
                                    if (exists) {
                                        return Mono.error(new BusinessError("errors.approval.already.exists",
                                                Integer.toString(employeeId),
                                                Integer.toString(periodId),
                                                auth.getUsername()));
                                    }
                                    return Mono.just(report);
                                }).flatMap(r -> {
                                    // 5. Save approval decision
                                    var approvalEntry = new OvertimeApprovalDecisionEntry();
                                    approvalEntry.setApprover(auth.getEmployeeInfo().getEmployeeId());
                                    approvalEntry.setReportId(report.getId());
                                    approvalEntry.setDecisionTime(dateTimeService.now());
                                    approvalEntry.setDecision(decision);
                                    approvalEntry.setComment(comment);
                                    return approvalRepo.save(approvalEntry);
                                    // 6. Just reload whole report to populate all required fields for approval entry
                                }).flatMap(approvalEntry -> get(employeeId, periodId)))));
    }


    /**
     * Public method accessible to everyone
     *
     * @return
     */
    public Flux<OvertimeClosedPeriodDto> getClosedPeriods() {
        return closedPeriodRepo.allClosed().map(mapper::fromEntry);
    }

    private Mono<OvertimeApprovalDecisionEntry> cancelPreviousApproval(Integer previousApprovalId, AuthContext auth) {
        if (previousApprovalId == null) {
            return Mono.empty();
        }
        return approvalRepo.findById(previousApprovalId).flatMap(approval -> {
            approval.setCancelDecisionTime(dateTimeService.now());
            approval.setComment("Canceled because of new approval added");
            return approvalRepo.save(approval);
        }).switchIfEmpty(Mono.error(new BusinessError("errors.approval.not.found", Integer.toString(previousApprovalId))));
    }


    private OvertimeReportDto stub(int employeeId, int periodId) {
        var stub = new OvertimeReportDto();
        stub.setEmployeeId(employeeId);
        stub.setPeriod(periodId);
        return stub;
    }

    private OvertimeReportEntry stubEntry(int employeeId, int periodId) {
        var stub = new OvertimeReportEntry();
        stub.setEmployeeId(employeeId);
        stub.setPeriod(periodId);
        return stub;
    }

    private Mono<OvertimeReportDto> get(int employeeId, int periodId) {
        // 1. Get report entry
        return reportRepo.get(employeeId, periodId)
                // 2. Map to DTO
                .map(r -> mapper.reportToDto(r))
                // 3. Get items
                .flatMap(report -> itemRepo.get(report.getId())
                        // 4. Map items to dto and collect to list
                        .map(item -> mapper.itemToDto(item)).collectList()
                        // 5. Return empty list if no items found
                        .defaultIfEmpty(new ArrayList<>())
                        // 6. Populate items in report
                        .map(items -> {
                            report.setItems(items);
                            return report;
                        }))
                // 7. Load all approvals
                .flatMap(report -> approvalRepo.findNotCanceledByReportId(report.getId())
                        .map(approvalEntry -> mapper.approvalToDtoIncludeOutdated(approvalEntry, report)).collectList()
                        .defaultIfEmpty(new ArrayList<>())
                        // 8. Populate report with approvals
                        .map(approvals -> {
                            report.setApprovals(approvals);
                            return report;
                        })
                );
    }


    private Mono<Boolean> validatePeriodNotClosed(int period) {
        return closedPeriodRepo.findById(period)
                .flatMap(p -> Mono.error(new BusinessError("errors.overtime.period.closed", Integer.toString(p.getPeriod()))))
                .map(p -> false)
                .defaultIfEmpty(true);
    }

}
