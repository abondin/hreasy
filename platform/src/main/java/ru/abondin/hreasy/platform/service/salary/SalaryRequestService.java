package ru.abondin.hreasy.platform.service.salary;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.assessment.AssessmentRepo;
import ru.abondin.hreasy.platform.repo.employee.EmployeeDetailedRepo;
import ru.abondin.hreasy.platform.repo.history.HistoryEntry;
import ru.abondin.hreasy.platform.repo.salary.SalaryRequestApprovalRepo;
import ru.abondin.hreasy.platform.repo.salary.SalaryRequestClosedPeriodRepo;
import ru.abondin.hreasy.platform.repo.salary.SalaryRequestRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.HistoryDomainService;
import ru.abondin.hreasy.platform.service.salary.dto.SalaryRequestClosedPeriodDto;
import ru.abondin.hreasy.platform.service.salary.dto.SalaryRequestDto;
import ru.abondin.hreasy.platform.service.salary.dto.SalaryRequestMapper;
import ru.abondin.hreasy.platform.service.salary.dto.SalaryRequestReportBody;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalaryRequestService {
    private final SalaryRequestRepo requestRepo;

    private final SalaryRequestClosedPeriodRepo closedPeriodRepo;
    private final SalaryRequestApprovalRepo approvalRepo;
    private final SalarySecurityValidator secValidator;
    private final SalaryRequestMapper mapper;

    private final HistoryDomainService historyDomainService;

    private final DateTimeService dateTimeService;

    private final AssessmentRepo assessmentRepo;

    private final EmployeeDetailedRepo employeeRepo;

    public Mono<SalaryRequestDto> get(AuthContext auth, int id) {
        log.debug("Getting salary request {} by {}", id, auth);
        return requestRepo.findFullNotDeletedById(id, dateTimeService.now())
                .switchIfEmpty(Mono.error(new BusinessError("errors.entity.not.found", Integer.toString(id))))
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
    public Flux<SalaryRequestDto> findMy(AuthContext auth, int periodId) {
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
    public Mono<? extends Integer> delete(AuthContext auth, int requestId) {
        log.info("Deleting salary request {} by {}", requestId, auth.getUsername());
        var now = dateTimeService.now();
        var deletedBy = auth.getEmployeeInfo().getEmployeeId();
        // 1. Find entity to delete
        return requestRepo.findById(requestId)
                .switchIfEmpty(Mono.error(new BusinessError("errors.entity.not.found", Integer.toString(requestId))))
                // 2. Check that period is not closed
                .flatMap(entry -> closedPeriodCheck(entry.getReqIncreaseStartPeriod())
                        // 3. Validate if user has permissions to delete
                        .flatMap(e -> secValidator.validateDeleteSalaryRequest(auth, entry)
                                .flatMap(s -> {
                                    // 4. Validate that request is not implemented
                                    if (entry.getImplementedAt() != null) {
                                        return Mono.error(new BusinessError("errors.salary_request.already_implemented", Integer.toString(requestId)));
                                    }
                                    // 5. Update deleted fields
                                    entry.setDeletedAt(now);
                                    entry.setDeletedBy(deletedBy);
                                    return requestRepo.save(entry);
                                }))).flatMap(deleted ->
                        // 6. Save history
                        historyDomainService.persistHistory(requestId, HistoryDomainService.HistoryEntityType.SALARY_REQUEST, deleted, now, deletedBy)
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
                .flatMap(v -> checkReportBodyConsistency(ctx, body))
                // 3. Get additional information about employee
                .flatMap(v -> employeeRepo.findDetailed(body.getEmployeeId()))
                .map(empl -> mapper.toEntry(body, empl, createdBy, now)).flatMap(entry -> {
                            // 2. Save new request to DB
                            return requestRepo.save(entry).flatMap(persisted ->
                                    // 3. Save history record
                                    historyDomainService.persistHistory(persisted.getId(),
                                                    HistoryDomainService.HistoryEntityType.SALARY_REQUEST,
                                                    persisted, now, createdBy)
                                            .map(h -> persisted.getId())
                            );
                        }
                );
        // 4. TODO Send email notification
    }

    private Mono<Boolean> closedPeriodCheck(int periodId) {
        return closedPeriodRepo.findById(periodId)
                .flatMap(p -> Mono.error(new BusinessError("errors.salary_request.period_closed", Integer.toString(p.getPeriod()))))
                .map(e -> true)
                .defaultIfEmpty(true);
    }

    private Mono<Boolean> checkReportBodyConsistency(AuthContext ctx, SalaryRequestReportBody body) {
        // 1. Check if report period is not closed
        var closedPeriodCheck = closedPeriodCheck(body.getIncreaseStartPeriod());


        // 2. Check if assessment for the same employee
        var assessmentCorrect = body.getAssessmentId() == null ? Mono.defer(() -> Mono.just(true)) :
                assessmentRepo.findById(body.getAssessmentId())
                        .switchIfEmpty(Mono.error(new BusinessError("errors.entity.not.found", Integer.toString(body.getAssessmentId()))))
                        .flatMap(assessment -> {
                            if (assessment.getEmployee() == null || !assessment.getEmployee().equals(body.getEmployeeId())) {
                                return Mono.error(new BusinessError("errors.entity.not.found", Integer.toString(body.getAssessmentId())));
                            }
                            return Mono.just(true);
                        });
        return closedPeriodCheck.flatMap(v -> assessmentCorrect);
    }

    public Flux<SalaryRequestClosedPeriodDto> getClosedSalaryRequestPeriods(AuthContext auth) {
        return closedPeriodRepo.findAll().map(mapper::closedPeriodFromEntry);
    }


}
