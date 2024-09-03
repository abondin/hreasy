package ru.abondin.hreasy.platform.service.salary;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.BusinessErrorFactory;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.assessment.AssessmentRepo;
import ru.abondin.hreasy.platform.repo.employee.EmployeeDetailedRepo;
import ru.abondin.hreasy.platform.repo.history.HistoryEntry;
import ru.abondin.hreasy.platform.repo.salary.*;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.HistoryDomainService;
import ru.abondin.hreasy.platform.service.salary.dto.*;
import ru.abondin.hreasy.platform.service.salary.dto.approval.SalaryRequestApprovalDto;
import ru.abondin.hreasy.platform.service.salary.dto.approval.SalaryRequestApproveBody;
import ru.abondin.hreasy.platform.service.salary.dto.approval.SalaryRequestCommentBody;
import ru.abondin.hreasy.platform.service.salary.dto.approval.SalaryRequestDeclineBody;

import java.time.OffsetDateTime;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalaryRequestService {
    public static final String SALARY_REQUEST_ENTITY_TYPE = "SalaryRequest";
    private final SalaryRequestRepo requestRepo;

    private final SalaryRequestClosedPeriodRepo closedPeriodRepo;
    private final SalaryRequestApprovalRepo approvalRepo;
    private final SalarySecurityValidator secValidator;
    private final SalaryRequestMapper mapper;

    private final HistoryDomainService historyDomainService;

    private final DateTimeService dateTimeService;

    private final AssessmentRepo assessmentRepo;

    private final EmployeeDetailedRepo employeeRepo;

    //<editor-fold desc="Common operations">

    public Mono<SalaryRequestDto> get(AuthContext auth, int id) {
        log.debug("Getting salary request {} by {}", id, auth);
        return requestRepo.findFullNotDeletedById(id, dateTimeService.now())
                .switchIfEmpty(BusinessErrorFactory.entityNotFound(SALARY_REQUEST_ENTITY_TYPE, id))
                .flatMap(entry -> secValidator.validateViewSalaryRequest(auth, entry).map(v -> mapper.fromEntry(entry)));
    }

    /**
     * @param auth
     * @param periodId
     * @return not deleted salary requests for given period filtered by logged-in permissions:
     * <ul>
     *     <li>Only if user has permission "approve_salary_request" and access to request's budgeting business account</>
     *     <li>(if user has "report_salary_request" permission) requests created by logged in user</li>
     * </ul>
     */
    public Flux<SalaryRequestDto> find(AuthContext auth, int periodId) {
        var now = dateTimeService.now();
        var authEmpl = auth.getEmployeeInfo();
        log.debug("Get all accessible requests for period {} by {}", periodId, auth);
        return secValidator.validateView(auth)
                .flatMapMany(v -> switch (v) {
                    case FROM_MY_BAS -> authEmpl.getAccessibleBas().isEmpty() ? requestRepo.findNotDeletedMy(
                            periodId, authEmpl.getEmployeeId(), now) : requestRepo.findNotDeleted(
                            periodId, authEmpl.getEmployeeId(), authEmpl.getAccessibleBas(), now);
                    case ONLY_MY -> requestRepo.findNotDeletedMy(
                            periodId, authEmpl.getEmployeeId(), now);
                })
                .map(mapper::fromEntry);
    }

    @Transactional
    public Mono<Integer> delete(AuthContext auth, int requestId) {
        log.info("Deleting salary request {} by {}", requestId, auth.getUsername());
        var now = dateTimeService.now();
        var currentUser = auth.getEmployeeInfo().getEmployeeId();
        return doUpdateOrDelete(auth, requestId, entry -> {
            entry.setDeletedAt(now);
            entry.setDeletedBy(currentUser);
        });
    }

    @Transactional
    public Mono<Integer> update(AuthContext auth, int requestId, SalaryRequestUpdateBody body) {
        log.info("Update salary request {} by {}", requestId, auth.getUsername());
        return doUpdateOrDelete(auth, requestId, entry ->
                mapper.applyRequestUpdateBody(entry, body)
        );
    }

    private Mono<Integer> doUpdateOrDelete(AuthContext auth, int requestId, Consumer<SalaryRequestEntry> modifications) {
        var now = dateTimeService.now();
        var currentUser = auth.getEmployeeInfo().getEmployeeId();
        // 1. Find entity to update/delete
        return requestRepo.findById(requestId)
                .switchIfEmpty(BusinessErrorFactory.entityNotFound(SALARY_REQUEST_ENTITY_TYPE, requestId))
                // 2. Check that period is not closed
                .flatMap(entry -> checkDeleteOrUpdateActionAllowed(entry)
                        // 3. Validate if user has permissions to update/delete
                        .flatMap(e -> secValidator.validateUpdateOrDeleteSalaryRequest(auth, entry)
                                .flatMap(s -> {
                                    modifications.accept(entry);
                                    return requestRepo.save(entry);
                                }))).flatMap(updatedItem ->
                        // 5. Save history
                        historyDomainService.persistHistory(requestId, HistoryDomainService.HistoryEntityType.SALARY_REQUEST, updatedItem, now, currentUser)
                ).map(HistoryEntry::getEntityId);

    }

    @Transactional
    public Mono<Integer> report(AuthContext ctx, SalaryRequestReportBody body) {
        var now = dateTimeService.now();
        var createdBy = ctx.getEmployeeInfo().getEmployeeId();
        log.info("Reporting {} by {}", body, ctx.getUsername());
        // 1. Validate if logged-in user has permissions to report new salary request
        return secValidator.validateReportSalaryRequest(ctx)
                // 2. Additional validation
                .flatMap(v -> checkReportBodyConsistency(body))
                // 3. Get additional information about employee
                .flatMap(v -> employeeRepo.findDetailed(body.getEmployeeId()))
                .map(empl -> mapper.toEntry(body, empl, createdBy, now)).flatMap(entry ->
                        // 2. Save new request to DB
                        requestRepo.save(entry).flatMap(persisted ->
                                // 3. Save history record
                                historyDomainService.persistHistory(persisted.getId(),
                                                HistoryDomainService.HistoryEntityType.SALARY_REQUEST,
                                                persisted, now, createdBy)
                                        .map(h -> persisted.getId()))

                );
        // 4. TODO Send email notification
    }
    //</editor-fold>

    //<editor-fold desc="Approval">

    public Flux<SalaryRequestApprovalDto> findApprovals(AuthContext auth, int requestId) {
        log.info("Get approvals for salary request {} by {}", requestId, auth.getUsername());
        return requestRepo.findById(requestId)
                .switchIfEmpty(BusinessErrorFactory.entityNotFound(SALARY_REQUEST_ENTITY_TYPE, requestId))
                .flatMap(request -> secValidator.validateApproveSalaryRequest(auth, request.getBudgetBusinessAccount()))
                .flatMapMany(v -> approvalRepo.findNotDeletedByRequestId(requestId, OffsetDateTime.now()).map(mapper::fromEntry));
    }

    @Transactional
    public Mono<Integer> approve(AuthContext auth, int requestId, SalaryRequestApproveBody body) {
        log.info("Approve salary request {} by {}", requestId, auth.getUsername());
        return this.doProcessApprovalAction(auth, requestId, SalaryRequestApprovalDto.ApprovalActionTypes.APPROVE.getValue(), body.getComment());
    }

    @Transactional
    public Mono<Integer> decline(AuthContext auth, int requestId, SalaryRequestDeclineBody body) {
        log.info("Decline salary request {} by {}", requestId, auth.getUsername());
        return this.doProcessApprovalAction(auth, requestId, SalaryRequestApprovalDto.ApprovalActionTypes.DECLINE.getValue(), body.getComment());
    }

    @Transactional
    public Mono<Integer> comment(AuthContext auth, int requestId, SalaryRequestCommentBody body) {
        log.info("Comment salary request {} by {}", requestId, auth.getUsername());
        return this.doProcessApprovalAction(auth, requestId, SalaryRequestApprovalDto.ApprovalActionTypes.COMMENT.getValue(), body.getComment());
    }

    private Mono<Integer> doProcessApprovalAction(AuthContext auth, int requestId, short state, String comment) {
        var now = dateTimeService.now();
        // 1. Get request
        return requestRepo.findById(requestId)
                .switchIfEmpty(BusinessErrorFactory.entityNotFound(SALARY_REQUEST_ENTITY_TYPE, requestId))
                // 2. Validate security
                .flatMap(entry -> secValidator.validateApproveSalaryRequest(auth, entry.getBudgetBusinessAccount())
                        // 3. Check if report period is not closed
                        .flatMap(v -> checkApprovalActionAllowed(entry, state))
                        // 4. Apply action
                        .flatMap(v -> {
                            var approvalEntry = new SalaryRequestApprovalEntry();
                            approvalEntry.setRequestId(requestId);
                            approvalEntry.setState(state);
                            approvalEntry.setCreatedBy(auth.getEmployeeInfo().getEmployeeId());
                            approvalEntry.setCreatedAt(now);
                            approvalEntry.setComment(comment);

                            return approvalRepo.save(approvalEntry).flatMap(persistedEntry ->
                                    // 4. Save history
                                    historyDomainService.persistHistory(
                                            persistedEntry.getId(),
                                            HistoryDomainService.HistoryEntityType.SALARY_REQUEST_APPROVAL,
                                            persistedEntry, now, auth.getEmployeeInfo().getEmployeeId())
                            ).map(HistoryEntry::getEntityId);
                        }));

    }

    @Transactional
    public Mono<Integer> deleteApproval(AuthContext auth, int requestId, int approvalId) {
        log.info("Deleting approval {} for salary request {} by {}", approvalId, requestId, auth.getUsername());
        // 1. Find approval
        return approvalRepo.findById(approvalId)
                .switchIfEmpty(BusinessErrorFactory.entityNotFound("SalaryRequestApproval", approvalId))
                .flatMap(approvalEntry ->
                        // 2. Check if user can delete approval
                        secValidator.validateDeleteApproval(auth, approvalEntry)
                                // 3. Find approval request
                                .flatMap(v -> requestRepo.findById(requestId).switchIfEmpty(BusinessErrorFactory.entityNotFound(SALARY_REQUEST_ENTITY_TYPE, requestId))
                                        // 4. Check if period is not closed
                                        .flatMap(e -> checkApprovalActionAllowed(e, approvalEntry.getState())))
                                .flatMap(v -> {
                                    // 5. Check that approval is related to given request
                                    if (requestId != approvalEntry.getRequestId()) {
                                        return Mono.error(new BusinessError("errors.salary_request.approval.not_for_request", Integer.toString(approvalId), Integer.toString(requestId)));
                                    }
                                    // 6. Update approval in db
                                    approvalEntry.setDeletedAt(OffsetDateTime.now());
                                    approvalEntry.setDeletedBy(auth.getEmployeeInfo().getEmployeeId());
                                    return approvalRepo.save(approvalEntry).flatMap(persisted -> historyDomainService.persistHistory(
                                                    persisted.getId(),
                                                    HistoryDomainService.HistoryEntityType.SALARY_REQUEST_APPROVAL,
                                                    persisted, dateTimeService.now(), auth.getEmployeeInfo().getEmployeeId())
                                            .map(HistoryEntry::getEntityId
                                            ));
                                }));
    }

    //</editor-fold>

    private Mono<Boolean> closedPeriodCheck(int periodId) {
        return closedPeriodRepo.findById(periodId)
                .flatMap(p -> Mono.error(new BusinessError("errors.salary_request.period_closed", Integer.toString(p.getPeriod()))))
                .map(e -> true)
                .defaultIfEmpty(true);
    }

    private Mono<Boolean> checkReportBodyConsistency(SalaryRequestReportBody body) {
        // 1. Check if report period is not closed
        var closedPeriodCheck = closedPeriodCheck(body.getIncreaseStartPeriod());


        // 2. Check if assessment for the same employee
        var assessmentCorrect = body.getAssessmentId() == null ? Mono.defer(() -> Mono.just(true)) :
                assessmentRepo.findById(body.getAssessmentId())
                        .switchIfEmpty(BusinessErrorFactory.entityNotFound("Assessment", body.getAssessmentId()))
                        .flatMap(assessment -> {
                            if (assessment.getEmployee() == null || !assessment.getEmployee().equals(body.getEmployeeId())) {
                                return BusinessErrorFactory.entityNotFound("Assessment", body.getAssessmentId());
                            }
                            return Mono.just(true);
                        });
        return closedPeriodCheck.flatMap(v -> assessmentCorrect);
    }

    private Mono<Boolean> checkApprovalActionAllowed(SalaryRequestEntry entry, short approvalState) {
        // 1. Allow even if request is marked as implemented
        // Add and delete comments is allowed even in closed period
        if (approvalState == SalaryRequestApprovalDto.ApprovalActionTypes.COMMENT.getValue()) {
            return Mono.just(true);
        }
        return closedPeriodCheck(entry.getReqIncreaseStartPeriod());
    }

    private Mono<Boolean> checkDeleteOrUpdateActionAllowed(SalaryRequestEntry entry) {
        // 1. Check if report period is not closed
        var closedPeriodCheck = closedPeriodCheck(entry.getReqIncreaseStartPeriod());
        // 2. Check that request is not implemented
        var implementedCheck = Mono.defer(() -> entry.getImplementedAt() == null
                ? Mono.just(true)
                : Mono.error(new BusinessError("errors.salary_request.already_implemented", Integer.toString(entry.getId()))));
        return closedPeriodCheck.flatMap(v -> implementedCheck);
    }

    public Flux<SalaryRequestClosedPeriodDto> getClosedSalaryRequestPeriods() {
        return closedPeriodRepo.findAll().map(mapper::closedPeriodFromEntry);
    }


}
