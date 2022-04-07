package ru.abondin.hreasy.platform.service.admin.employee;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.employee.admin.EmployeeHistoryRepo;
import ru.abondin.hreasy.platform.repo.employee.admin.EmployeeWithAllDetailsEntry;
import ru.abondin.hreasy.platform.repo.employee.admin.EmployeeWithAllDetailsRepo;
import ru.abondin.hreasy.platform.repo.employee.admin.kids.EmployeeKidEntry;
import ru.abondin.hreasy.platform.repo.employee.admin.kids.EmployeeKidRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.admin.AdminSecurityValidator;
import ru.abondin.hreasy.platform.service.admin.employee.dto.*;

import java.time.OffsetDateTime;
import java.time.Period;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminEmployeeService {
    private final DateTimeService dateTimeService;
    private final EmployeeWithAllDetailsRepo employeeRepo;
    private final EmployeeHistoryRepo historyRepo;
    private final AdminSecurityValidator securityValidator;
    private final EmployeeAllFieldsMapper mapper;
    private final EmployeeKidRepo kidsRepo;


    private final static EmployeeWithAllDetailsEntry EMPTY_INSTANCE = new EmployeeWithAllDetailsEntry();

    public Flux<EmployeeWithAllDetailsDto> findAll(AuthContext auth) {
        return securityValidator.validateViewEmployeeFull(auth)
                .flatMapMany(sec -> employeeRepo.findAll()).map(m -> mapper.fromEntry(m, dateTimeService.now()));
    }

    public Flux<EmployeeKidDto> findAllKids(AuthContext auth) {
        return securityValidator.validateViewEmployeeFull(auth)
                .flatMapMany(sec -> kidsRepo.findAllKidsWithParentInfo(dateTimeService.now())).map(m -> {
                    var result = mapper.fromEntry(m);
                    result.setAge(m.getBirthday() == null ? null :
                            Period.between(m.getBirthday(), dateTimeService.now().toLocalDate()).getYears()
                    );
                    return result;
                });
    }

    public Mono<EmployeeWithAllDetailsDto> get(AuthContext auth, int employeeId) {
        return securityValidator.validateViewEmployeeFull(auth)
                .flatMap(sec -> employeeRepo.findById(employeeId)).map(m -> mapper.fromEntry(m, dateTimeService.now()));
    }

    @Transactional
    public Mono<Integer> create(AuthContext auth, CreateOrUpdateEmployeeBody body) {
        log.info("Create new employee {} by {}", body, auth.getUsername());
        var now = dateTimeService.now();
        return securityValidator.validateEditEmployee(auth)
                .flatMap(s -> employeeRepo.findByEmail(body.getEmail()))
                .defaultIfEmpty(EMPTY_INSTANCE)
                .flatMap(existing -> {
                    if (existing == EMPTY_INSTANCE) {
                        return doUpdate(auth.getEmployeeInfo().getEmployeeId(), now, new EmployeeWithAllDetailsEntry(), body);
                    } else {
                        return Mono.error(new BusinessError("errors.employeewithemail.exists", body.getEmail()));
                    }
                });
    }


    @Transactional
    public Mono<Integer> createNewKid(AuthContext auth, int employeeId, CreateOrUpdateEmployeeKidBody body) {
        log.info("Create new employee {} kid {} by {}", employeeId, body, auth.getUsername());
        var now = dateTimeService.now();
        return securityValidator.validateEditEmployee(auth)
                .flatMap(s -> employeeRepo.findById(employeeId))
                .switchIfEmpty(Mono.error(new BusinessError("errors.entity.not.found", Integer.toString(employeeId))))
                .flatMap(employee -> {
                    var kid = new EmployeeKidEntry();
                    kid.setBirthday(body.getBirthday());
                    kid.setDisplayName(body.getDisplayName());
                    kid.setParent(employeeId);
                    return kidsRepo.save(kid).map(EmployeeKidEntry::getId);
                });
    }

    @Transactional
    public Mono<Integer> updateKid(AuthContext auth, int employeeId, int kidId, CreateOrUpdateEmployeeKidBody body) {
        log.info("Update employee {} kid {}: {} by {}", employeeId, kidId, body, auth.getUsername());
        var now = dateTimeService.now();
        return securityValidator.validateEditEmployee(auth)
                .flatMap(s -> employeeRepo.findById(employeeId))
                .switchIfEmpty(Mono.error(new BusinessError("errors.entity.not.found", Integer.toString(employeeId))))
                .flatMap(s -> kidsRepo.findById(kidId))
                .switchIfEmpty(Mono.error(new BusinessError("errors.entity.not.found", Integer.toString(kidId))))
                .flatMap(kidEntry -> {
                    if (employeeId != kidEntry.getParent()) {
                        return Mono.error(new BusinessError("errors.entity.invalid.parent", Integer.toString(kidId),
                                Integer.toString(employeeId)));
                    }
                    kidEntry.setBirthday(body.getBirthday());
                    kidEntry.setDisplayName(body.getDisplayName());
                    return kidsRepo.save(kidEntry).map(EmployeeKidEntry::getId);
                });
    }

    @Transactional
    public Mono<Integer> update(AuthContext auth, int employeeId, CreateOrUpdateEmployeeBody body) {
        log.info("Update new employee {} with {} by {}", employeeId, body, auth.getUsername());
        var now = dateTimeService.now();
        return securityValidator.validateEditEmployee(auth)
                .flatMap(s -> employeeRepo.findById(employeeId))
                .switchIfEmpty(Mono.error(new BusinessError("errors.entity.not.found", Integer.toString(employeeId))))
                .flatMap(entry -> {
                    if (!entry.getEmail().equals(body.getEmail())) {
                        return Mono.error(new BusinessError("errors.emailupdate.unsupported", entry.getEmail(), body.getEmail()));
                    }
                    return doUpdate(auth.getEmployeeInfo().getEmployeeId(), now, entry, body);
                });
    }


    private Mono<Integer> doUpdate(int currentEmployeeId, OffsetDateTime now, EmployeeWithAllDetailsEntry entry, CreateOrUpdateEmployeeBody body) {
        mapper.populateFromBody(entry, body);
        return employeeRepo.save(entry).flatMap(persisted -> {
            var history = mapper.history(persisted, currentEmployeeId, now);
            return historyRepo.save(history).map(e -> persisted.getId());
        });
    }


}