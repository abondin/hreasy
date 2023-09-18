package ru.abondin.hreasy.platform.service.salary;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.salary.SalaryRequestApprovalRepo;
import ru.abondin.hreasy.platform.repo.salary.SalaryRequestRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.HistoryDomainService;
import ru.abondin.hreasy.platform.service.salary.dto.SalaryRequestDto;
import ru.abondin.hreasy.platform.service.salary.dto.SalaryRequestMapper;
import ru.abondin.hreasy.platform.service.salary.dto.SalaryRequestReportBody;
import ru.abondin.hreasy.platform.service.salary.dto.SalaryRequestReportStat;

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

    public Mono<SalaryRequestDto> get(AuthContext auth, int id) {
        return requestRepo.findFullById(id)
                .switchIfEmpty(Mono.error(new BusinessError("errors.entity.not.found", Integer.toString(id))))
                .flatMap(entry -> secValidator.validateViewSalaryRequest(auth, entry).map(v -> mapper.fromEntry(entry)));
    }

    @Transactional
    public Mono<Integer> report(AuthContext ctx, SalaryRequestReportBody body) {
        var now = dateTimeService.now();
        var createdBy = ctx.getEmployeeInfo().getEmployeeId();
        log.info("Reporting {} by {}", body, ctx.getUsername());
        // 1. Validate if logged-in user has permissions to report new salary request
        return secValidator.validateReportSalaryRequest(ctx)
                .map(v -> {
                    var entry = mapper.toEntry(body, createdBy, now);
                    entry.setStat(SalaryRequestReportStat.CREATED.getValue());
                    return entry;
                }).flatMap(entry -> {
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
}
