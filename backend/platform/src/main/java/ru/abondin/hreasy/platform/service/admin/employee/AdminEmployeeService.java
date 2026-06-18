package ru.abondin.hreasy.platform.service.admin.employee;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.BusinessErrorFactory;
import ru.abondin.hreasy.platform.api.employee.UpdateCurrentProjectBody;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.employee.admin.EmployeeHistoryRepo;
import ru.abondin.hreasy.platform.repo.employee.admin.EmployeeWithAllDetailsEntry;
import ru.abondin.hreasy.platform.repo.employee.admin.EmployeeWithAllDetailsRepo;
import ru.abondin.hreasy.platform.repo.employee.admin.kids.EmployeeKidEntry;
import ru.abondin.hreasy.platform.repo.employee.admin.kids.EmployeeKidRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.FileStorage;
import ru.abondin.hreasy.platform.service.admin.AdminSecurityValidator;
import ru.abondin.hreasy.platform.service.admin.employee.dto.*;
import ru.abondin.hreasy.platform.service.dto.EmployeeUpdateTelegramBody;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminEmployeeService {

    private static final EmployeeWithAllDetailsEntry EMPTY_INSTANCE = new EmployeeWithAllDetailsEntry();
    public static final String CURRENT_PROJECT_TRANSFER_APPROVAL_REQUIRED = "errors.current_project.transfer_approval_required";
    public static final String EMPLOYEE_ENTITY_TYPE = "employee";
    public static final String KID_ENTITY_TYPE = "kid";

    private final DateTimeService dateTimeService;
    private final EmployeeWithAllDetailsRepo employeeRepo;
    private final EmployeeHistoryRepo historyRepo;
    private final AdminSecurityValidator securityValidator;
    private final EmployeeAllFieldsMapper mapper;
    private final EmployeeKidRepo kidsRepo;
    private final FileStorage fileStorage;


    public Flux<EmployeeWithAllDetailsDto> findAll(AuthContext auth, boolean includeFired) {
        log.info("Get full information for all employees by {}", auth.getUsername());
        return securityValidator.validateViewEmployeeFull(auth)
                .flatMapMany(sec -> employeeRepo.findAllDetailed())
                .map(m -> mapper.fromView(m, hasAvatar(m.getId()), dateTimeService.now()))
                .filter(e -> includeFired || e.isActive());
    }

    public Flux<EmployeeKidDto> findAllKids(AuthContext auth) {
        log.info("Get all kids about employee by {}", auth.getUsername());
        var now = dateTimeService.now();
        return securityValidator.validateViewEmployeeFull(auth)
                .flatMapMany(sec -> kidsRepo.findAllKidsWithParentInfo(now))
                .map(m -> mapper.fromEntry(m, now));
    }

    public Mono<EmployeeKidDto> getKid(AuthContext auth, Integer employeeId, Integer employeeKidId) {
        log.debug("Get kid by id {} by {}", employeeKidId, auth.getUsername());
        return securityValidator.validateViewEmployeeFull(auth)
                .flatMap(sec -> kidsRepo.getFullInfo(employeeId, employeeKidId, dateTimeService.now()))
                .map(m -> mapper.fromEntry(m, dateTimeService.now()));
    }


    public Mono<EmployeeWithAllDetailsDto> get(AuthContext auth, int employeeId) {
        log.info("Get all information about employee {} by {}", employeeId, auth.getUsername());
        return securityValidator.validateViewEmployeeFull(auth)
                .flatMap(sec -> employeeRepo.findDetailedById(employeeId)).map(m ->
                        mapper.fromView(m, hasAvatar(employeeId), dateTimeService.now()));
    }

    @Transactional
    public Mono<Integer> create(AuthContext auth, CreateOrUpdateEmployeeBody body) {
        log.info("Create new employee {} by {}", body, auth.getUsername());
        var now = dateTimeService.now();
        return securityValidator.validateEditEmployee(auth)
                .flatMap(s -> employeeRepo.findByEmailsInLowerCase(body.getEmail()))
                .defaultIfEmpty(EMPTY_INSTANCE)
                .flatMap(existing -> {
                    if (existing == EMPTY_INSTANCE) {
                        return doUpdateFromBody(auth.getEmployeeInfo().getEmployeeId(), now, new EmployeeWithAllDetailsEntry(), body);
                    } else {
                        return Mono.error(new BusinessError("errors.employeewithemail.exists", body.getEmail()));
                    }
                });
    }


    @Transactional
    public Mono<Integer> createNewKid(AuthContext auth, int employeeId, CreateOrUpdateEmployeeKidBody body) {
        log.info("Create new employee {} kid {} by {}", employeeId, body, auth.getUsername());
        return securityValidator.validateEditEmployee(auth)
                .flatMap(s -> employeeRepo.findById(employeeId))
                .switchIfEmpty(BusinessErrorFactory.entityNotFound(EMPLOYEE_ENTITY_TYPE, employeeId))
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
        return securityValidator.validateEditEmployee(auth)
                .flatMap(s -> employeeRepo.findById(employeeId))
                .switchIfEmpty(BusinessErrorFactory.entityNotFound(EMPLOYEE_ENTITY_TYPE, employeeId))
                .flatMap(s -> kidsRepo.findById(kidId))
                .switchIfEmpty(BusinessErrorFactory.entityNotFound(KID_ENTITY_TYPE, kidId))
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
        log.info("Update employee {} with {} by {}", employeeId, body, auth.getUsername());
        var now = dateTimeService.now();
        return securityValidator.validateEditEmployee(auth)
                .flatMap(s -> employeeRepo.findById(employeeId))
                .switchIfEmpty(BusinessErrorFactory.entityNotFound(EMPLOYEE_ENTITY_TYPE, employeeId))
                .flatMap(entry -> {
                    if (!entry.getEmail().equals(body.getEmail())) {
                        return Mono.error(new BusinessError("errors.emailupdate.unsupported", entry.getEmail(), body.getEmail()));
                    }
                    // Reset telegram validation if account changed
                    if (!Objects.equals(entry.getTelegram(), body.getTelegram())) {
                        entry.setTelegramConfirmedAt(null);
                    }
                    return doUpdateFromBody(auth.getEmployeeInfo().getEmployeeId(), now, entry, body);
                });
    }

    @Transactional
    public Mono<Integer> updateTelegram(AuthContext auth, int employeeId, EmployeeUpdateTelegramBody body) {
        log.info("Update telegram account {} with {} by {}", employeeId, body, auth.getUsername());
        var now = dateTimeService.now();
        return securityValidator.validateUpdateTelegram(auth, employeeId)
                .flatMap(s -> employeeRepo.findById(employeeId))
                .switchIfEmpty(Mono.error(new BusinessError("errors.entity.not.found", Integer.toString(employeeId))))
                .flatMap(entry -> {
                    entry.setTelegram(body.getTelegram());
                    entry.setTelegramConfirmedAt(null);
                    return doUpdate(auth.getEmployeeInfo().getEmployeeId(), now, entry, null);
                });
    }

    @Transactional
    public Mono<Integer> updateCurrentProject(int employeeId, @Nullable UpdateCurrentProjectBody newCurrentProject, AuthContext auth) {
        var now = dateTimeService.now();
        log.info("Update current project {} for employee {} " +
                "by {}", newCurrentProject == null ? "<RESET>" : newCurrentProject, employeeId, auth.getEmail());
        // Process: first try direct project update with current security rules.
        // If direct update is denied, check whether the user manages the target project but not the source project.
        // In that case return a business error code for the transfer approval flow; otherwise keep access denied.
        return securityValidator.validateUpdateCurrentProject(auth, employeeId, newCurrentProject == null ? null : newCurrentProject.getId())
                .onErrorResume(AccessDeniedException.class, error -> currentProjectTransferApprovalRequired(auth,
                        employeeId, newCurrentProject == null ? null : newCurrentProject.getId(), error))
                .flatMap(s -> employeeRepo.findById(employeeId))
                .switchIfEmpty(Mono.error(new BusinessError("errors.entity.not.found", Integer.toString(employeeId))))
                .flatMap(entry -> {
                    entry.setCurrentProjectId(newCurrentProject == null ? null : newCurrentProject.getId());
                    entry.setCurrentProjectRole(newCurrentProject == null ? null : newCurrentProject.getRole());
                    return doUpdate(auth.getEmployeeInfo().getEmployeeId(), now, entry, null);
                });
    }

    private Mono<Boolean> currentProjectTransferApprovalRequired(AuthContext auth,
                                                                 int employeeId,
                                                                 Integer newProject,
                                                                 AccessDeniedException originalError) {
        return securityValidator.findCurrentProjectTransferAccessGap(auth, employeeId, newProject)
                .flatMap(approvalRequired -> Mono.<Boolean>error(transferApprovalRequired(approvalRequired)))
                .switchIfEmpty(Mono.error(originalError));
    }

    private BusinessError transferApprovalRequired(AdminSecurityValidator.CurrentProjectTransferAccessGap approvalRequired) {
        var error = new BusinessError(CURRENT_PROJECT_TRANSFER_APPROVAL_REQUIRED);
        error.setAttrs(Map.of(
                "employeeId", Integer.toString(approvalRequired.employeeId()),
                "fromProjectId", Integer.toString(approvalRequired.fromProjectId()),
                "toProjectId", Integer.toString(approvalRequired.toProjectId())
        ));
        return error;
    }


    private Mono<Integer> doUpdateFromBody(int currentEmployeeId, OffsetDateTime now, EmployeeWithAllDetailsEntry entry, CreateOrUpdateEmployeeBody body) {
        mapper.populateFromBody(entry, body);
        return doUpdate(currentEmployeeId, now, entry, body.getImportProcessId());
    }

    private Mono<Integer> doUpdate(int currentEmployeeId, OffsetDateTime now, EmployeeWithAllDetailsEntry entry, @Nullable Integer importProcessId) {
        return employeeRepo.save(entry).flatMap(persisted -> {
            var history = mapper.history(persisted, currentEmployeeId, now);
            history.setImportProcess(importProcessId);
            return historyRepo.save(history).map(e -> persisted.getId());
        });
    }

    private boolean hasAvatar(int emplId){
        return fileStorage.fileExists("avatars", emplId + ".png");
    }

}
