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
import ru.abondin.hreasy.platform.repo.salary.SalaryRequestApprovalRepo;
import ru.abondin.hreasy.platform.repo.salary.SalaryRequestClosedPeriodRepo;
import ru.abondin.hreasy.platform.repo.salary.SalaryRequestRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.HistoryDomainService;
import ru.abondin.hreasy.platform.service.salary.dto.SalaryRequestClosedPeriodDto;
import ru.abondin.hreasy.platform.service.salary.dto.SalaryRequestDto;
import ru.abondin.hreasy.platform.service.salary.dto.SalaryRequestMapper;
import ru.abondin.hreasy.platform.service.salary.dto.SalaryRequestReportBody;

import java.util.List;

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
     *     <li>All requests for user with "admin_salary_request" permission</li>
     *     <li>Only if user has permission "approve_salary_request" and access to request's budgeting business account or request employee's department</>
     *     <li>(in any ways) requests created by logged in user</li>
     * </ul>
     */
    public Flux<SalaryRequestDto> findMy(AuthContext auth, int periodId) {
        var now = dateTimeService.now();
        var authEmplInfo = auth.getEmployeeInfo();
        log.debug("Get all accessible requests for period {} by {}", periodId, auth);
        return secValidator.validateView(auth)
                .flatMapMany(v -> switch (v) {
                    case FROM_MY_BA_OR_DEPARTMENTS -> requestRepo.findNotDeleted(
                            periodId, authEmplInfo.getEmployeeId(), authEmplInfo.getAccessibleBas(), authEmplInfo.getAccessibleDepartments(), now);
                    case ONLY_MY -> requestRepo.findNotDeleted(
                            periodId, authEmplInfo.getEmployeeId(), List.of(), List.of(), now);
                })
                .map(mapper::fromEntry);
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
                .map(v -> mapper.toEntry(body, createdBy, now)).flatMap(entry -> {
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

    private Mono<Boolean> checkReportBodyConsistency(AuthContext ctx, SalaryRequestReportBody body) {
        // 1. Check if report period is not closed
        var closedPeriodCheck = closedPeriodRepo.findById(body.getIncreaseStartPeriod())
                .flatMap(p -> Mono.error(new BusinessError("errors.salary_request.period_closed", Integer.toString(p.getPeriod()))))
                .defaultIfEmpty(true);


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
