package ru.abondin.hreasy.platform.service.salary;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.BusinessErrorFactory;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.assessment.AssessmentRepo;
import ru.abondin.hreasy.platform.repo.employee.EmployeeDetailedRepo;
import ru.abondin.hreasy.platform.repo.salary.*;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.HistoryDomainService;
import ru.abondin.hreasy.platform.service.salary.dto.SalaryRequestMapper;
import ru.abondin.hreasy.platform.service.salary.dto.SalaryRequestReportBody;
import ru.abondin.hreasy.platform.service.salary.dto.approval.SalaryRequestApprovalDto;
import ru.abondin.hreasy.platform.service.salary.dto.link.SalaryRequestLinkCreateBody;

/**
 * Middleware service with logic for salary report. Uses in {@link SalaryRequestService} and {@link AdminSalaryRequestService}
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SalaryRequestDomainService {

    private final DateTimeService dateTimeService;
    private final SalaryRequestRepo requestRepo;
    private final SalarySecurityValidator secValidator;
    private final SalaryRequestMapper mapper;
    private final EmployeeDetailedRepo employeeRepo;
    private final HistoryDomainService historyDomainService;
    private final AssessmentRepo assessmentRepo;
    private final SalaryRequestClosedPeriodRepo closedPeriodRepo;
    private final SalaryRequestLinkRepo linkRepo;


    /**
     * Report new salary request logic. Do not forget to wrap with @Transactional
     *
     * @param ctx
     * @param body
     * @return
     */
    Mono<Integer> doReport(AuthContext ctx, SalaryRequestReportBody body) {
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

    Mono<Integer> createLink(AuthContext ctx, SalaryRequestLinkCreateBody body) {
        log.info("Creating salary request link [{}-{}] of type {} by {}", body.getSource(), body.getDestination(), body.getType(), ctx.getUsername());
        var now = dateTimeService.now();
        var entry = new SalaryRequestLinkEntry();
        entry.setType(body.getType());
        entry.setSource(body.getSource());
        entry.setDestination(body.getDestination());
        entry.setComment(body.getComment());
        entry.setCreatedBy(ctx.getEmployeeInfo().getEmployeeId());
        entry.setCreatedAt(now);
        return validateLinkCreation(ctx, body).flatMap(v -> linkRepo.save(entry)).flatMap(persisted ->
                historyDomainService.persistHistory(persisted.getId(),
                                HistoryDomainService.HistoryEntityType.SALARY_REQUEST_LINK,
                                persisted, now, ctx.getEmployeeInfo().getEmployeeId())
                        .map(h -> persisted.getId()));
    }

    Mono<Boolean> validateLinkCreation(AuthContext ctx, SalaryRequestLinkCreateBody body) {
        return secValidator.validateReportSalaryRequest(ctx).flatMap(v -> Mono.defer(() -> {
            if (body.getSource() == body.getDestination()) {
                return Mono.error(new BusinessError("errors.salary_request.link.source_equals_destination"));
            }
            return Mono.just(true);

        }));

    }

    Mono<Boolean> checkReportBodyConsistency(SalaryRequestReportBody body) {
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

    Mono<Boolean> closedPeriodCheck(int periodId) {
        return closedPeriodRepo.findById(periodId)
                .flatMap(p -> Mono.error(new BusinessError("errors.salary_request.period_closed", Integer.toString(p.getPeriod()))))
                .map(e -> true)
                .defaultIfEmpty(true);
    }


    Mono<Boolean> checkApprovalActionAllowed(SalaryRequestEntry entry, short approvalState) {
        // 1. Allow even if request is marked as implemented
        // Add and delete comments is allowed even in closed period
        if (approvalState == SalaryRequestApprovalDto.ApprovalActionTypes.COMMENT.getValue()) {
            return Mono.just(true);
        }
        return closedPeriodCheck(entry.getReqIncreaseStartPeriod());
    }

    Mono<Boolean> checkDeleteOrUpdateActionAllowed(SalaryRequestEntry entry) {
        // 1. Check if report period is not closed
        var closedPeriodCheck = closedPeriodCheck(entry.getReqIncreaseStartPeriod());
        // 2. Check that request is not implemented
        var implementedCheck = Mono.defer(() -> entry.getImplementedAt() == null
                ? Mono.just(true)
                : Mono.error(new BusinessError("errors.salary_request.already_implemented", Integer.toString(entry.getId()))));
        return closedPeriodCheck.flatMap(v -> implementedCheck);
    }

}
