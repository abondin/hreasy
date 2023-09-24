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
import ru.abondin.hreasy.platform.repo.salary.SalaryRequestRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.HistoryDomainService;
import ru.abondin.hreasy.platform.service.salary.dto.SalaryRequestDto;
import ru.abondin.hreasy.platform.service.salary.dto.SalaryRequestMapper;
import ru.abondin.hreasy.platform.service.salary.dto.SalaryRequestReportBody;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalaryRequestService {
    private final SalaryRequestRepo requestRepo;
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

    public Flux<SalaryRequestDto> getMy(AuthContext auth) {
        log.debug("Getting my requests {}", auth);
        return secValidator.validateViewMySalaryRequest(auth)
                .flatMapMany(v -> requestRepo.findMy(auth.getEmployeeInfo().getEmployeeId(), dateTimeService.now()))
                .map(mapper::fromEntry);
    }

    public Flux<SalaryRequestDto> findAll(AuthContext auth) {
        log.info("Getting all salary requests by {}", auth);
        return secValidator.validateViewAllSalaryRequests(auth)
                .flatMapMany(v -> requestRepo.findAllNotDeleted(dateTimeService.now()))
                .map(mapper::fromEntry);
    }

    public Flux<SalaryRequestDto> findInBa(AuthContext auth, int baId) {
        log.debug("Getting salary request in ba {} by {}", baId, auth);
        return secValidator.validateViewSalaryRequestsOfBusinessAccount(auth, baId)
                .flatMapMany(v -> requestRepo.findByBA(baId, dateTimeService.now()))
                .map(mapper::fromEntry);
    }

    public Flux<SalaryRequestDto> findInDepartment(AuthContext auth, int departmentId) {
        log.debug("Getting salary request in department {} by {}", departmentId, auth);
        return secValidator.validateViewSalaryRequestsOfDepartment(auth, departmentId)
                .flatMapMany(v -> requestRepo.findByDepartment(departmentId, dateTimeService.now()))
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
        // 1. Check if assessment for the same employee
        return body.getAssessmentId() == null ? Mono.defer(() -> Mono.just(true)) :
                assessmentRepo.findById(body.getAssessmentId())
                        .switchIfEmpty(Mono.error(new BusinessError("errors.entity.not.found", Integer.toString(body.getAssessmentId()))))
                        .flatMap(assessment -> {
                            if (assessment.getEmployee() == null || !assessment.getEmployee().equals(body.getEmployeeId())) {
                                return Mono.error(new BusinessError("errors.entity.not.found", Integer.toString(body.getAssessmentId())));
                            }
                            return Mono.just(true);
                        });
    }

    @Transactional
    public Mono<Integer> moveToInProgress(AuthContext auth, int salaryRequestId) {
        log.info("Salary request {} moved to in progress by {}", salaryRequestId, auth.getUsername());
        var now = dateTimeService.now();
        return secValidator.validateMoveInProgressSalaryRequest(auth).flatMap(v ->
                        requestRepo.findFullNotDeletedById(salaryRequestId, now))
                .switchIfEmpty(Mono.error(new BusinessError("errors.entity.not.found", Integer.toString(salaryRequestId))))
                .flatMap(entry -> {
                    if (entry.getImplementedAt() != null) {
                        return Mono.error(new BusinessError("errors.salary_request.already_implemented", Integer.toString(salaryRequestId)));
                    }
                    if (entry.getInprogressAt() != null) {
                        return Mono.error(new BusinessError("errors.salary_request.already_inprogress", Integer.toString(salaryRequestId)));
                    }
                    return requestRepo.moveToInProgress(salaryRequestId, now, auth.getEmployeeInfo().getEmployeeId());
                });
    }

    @Transactional
    public Mono<Integer> markAsImplemented(AuthContext auth, int salaryRequestId) {
        log.info("Salary request {} marked to in progress by {}", salaryRequestId, auth.getUsername());
        var now = dateTimeService.now();
        return secValidator.validateMarkAsImplementedSalaryRequest(auth).flatMap(v ->
                        requestRepo.findFullNotDeletedById(salaryRequestId, now))
                .switchIfEmpty(Mono.error(new BusinessError("errors.entity.not.found", Integer.toString(salaryRequestId))))
                .flatMap(entry -> {
                    if (entry.getInprogressAt() == null) {
                        return Mono.error(new BusinessError("errors.salary_request.not_in_inprogress", Integer.toString(salaryRequestId)));
                    }
                    if (entry.getImplementedAt() != null) {
                        return Mono.error(new BusinessError("errors.salary_request.already_implemented", Integer.toString(salaryRequestId)));
                    }
                    return requestRepo.markAsImplemented(salaryRequestId, now, auth.getEmployeeInfo().getEmployeeId());
                });
    }


}
