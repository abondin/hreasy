package ru.abondin.hreasy.platform.service.admin.ba;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.ba.BusinessAccountAssignmentRepo;
import ru.abondin.hreasy.platform.repo.history.HistoryEntry;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.HistoryDomainService;
import ru.abondin.hreasy.platform.service.admin.ba.dto.BusinessAccountAssignmentDto;
import ru.abondin.hreasy.platform.service.admin.ba.dto.BusinessAccountAssignmentMapper;
import ru.abondin.hreasy.platform.service.admin.ba.dto.CreateBusinessAccountAssignmentBody;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminBaAssignmentService {

    private final BusinessAccountAssignmentRepo assignmentRepo;
    private final BusinessAccountAssignmentMapper mapper;
    private final AdminBaSecurityValidator securityValidator;
    private final DateTimeService dateTimeService;

    private final HistoryDomainService history;


    public Flux<BusinessAccountAssignmentDto> findInBusinessAccount(AuthContext auth, int baId, int period) {
        return securityValidator.validateAdminBaAssignments(auth, baId)
                .flatMapMany(v -> assignmentRepo.findInBusinessAccount(period, baId))
                .map(mapper::fromEntry);
    }

    @Transactional
    public Mono<Integer> create(AuthContext auth, int baId, int period, CreateBusinessAccountAssignmentBody body) {
        log.info("Creating new BusinessAccountAssignment baId={},period={},body={}, by {}", baId, period, body, auth.getUsername());
        var now = dateTimeService.now();
        var employee = auth.getEmployeeInfo().getEmployeeId();
        return securityValidator.validateAdminBaAssignments(auth, baId)
                .map(v -> mapper.toEntry(body, baId, period, employee, now))
                .flatMap(assignmentRepo::save)
                .flatMap(persisted -> history.persistHistory(
                        persisted.getId()
                        , HistoryDomainService.HistoryEntityType.BUSINESS_ACCOUNT_ASSIGNMENT,
                        persisted,
                        now, employee)).map(HistoryEntry::getEntityId);
    }
}