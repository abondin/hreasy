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
import ru.abondin.hreasy.platform.config.BackgroundTasksProps;
import ru.abondin.hreasy.platform.repo.employee.admin.EmployeeHistoryRepo;
import ru.abondin.hreasy.platform.repo.employee.admin.EmployeeWithAllDetailsEntry;
import ru.abondin.hreasy.platform.repo.employee.admin.EmployeeWithAllDetailsRepo;
import ru.abondin.hreasy.platform.repo.employee.admin.kids.EmployeeKidEntry;
import ru.abondin.hreasy.platform.repo.employee.admin.kids.EmployeeKidRepo;
import ru.abondin.hreasy.platform.repo.employee.projecttransfer.ProjectTransferRequestEntry;
import ru.abondin.hreasy.platform.repo.employee.projecttransfer.ProjectTransferRequestRepo;
import ru.abondin.hreasy.platform.repo.manager.ManagerRecipient;
import ru.abondin.hreasy.platform.repo.manager.ManagerRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.FileStorage;
import ru.abondin.hreasy.platform.service.HistoryDomainService;
import ru.abondin.hreasy.platform.service.admin.AdminSecurityValidator;
import ru.abondin.hreasy.platform.service.admin.employee.dto.*;
import ru.abondin.hreasy.platform.service.dto.EmployeeUpdateTelegramBody;
import ru.abondin.hreasy.platform.service.notification.NotificationOrchestrator;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminEmployeeService {

    private static final EmployeeWithAllDetailsEntry EMPTY_INSTANCE = new EmployeeWithAllDetailsEntry();
    public static final String CURRENT_PROJECT_TRANSFER_APPROVAL_REQUIRED = "errors.current_project.transfer_approval_required";
    public static final String CURRENT_PROJECT_TRANSFER_REQUEST_ALREADY_PENDING = "errors.current_project.transfer_request.already_pending";
    public static final String EMPLOYEE_ENTITY_TYPE = "employee";
    public static final String KID_ENTITY_TYPE = "kid";
    private static final List<String> CURRENT_PROJECT_TRANSFER_APPROVER_TYPES = List.of("project", "business_account", "department");

    private final DateTimeService dateTimeService;
    private final EmployeeWithAllDetailsRepo employeeRepo;
    private final EmployeeHistoryRepo historyRepo;
    private final AdminSecurityValidator securityValidator;
    private final EmployeeAllFieldsMapper mapper;
    private final EmployeeKidRepo kidsRepo;
    private final FileStorage fileStorage;
    private final ManagerRepo managerRepo;
    private final ProjectTransferRequestRepo projectTransferRequestRepo;
    private final HistoryDomainService historyDomainService;
    private final ProjectTransferRequestMapper projectTransferRequestMapper;
    private final NotificationOrchestrator notificationOrchestrator;
    private final BackgroundTasksProps backgroundTasksProps;


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
        // Process: if there is an active transfer request, block any direct project update.
        // It must be explicitly canceled first, even for users with update_current_project_global,
        // so we do not hide a pending approval flow behind an administrative update.
        // If there is no active request, first try direct project update with current security rules.
        // If direct update is denied, check whether the user manages the target project but not the source project.
        // In that case return a business error code for the transfer approval flow; otherwise keep access denied.
        return projectTransferRequestRepo.findPendingByEmployeeId(employeeId)
                .flatMap(_ -> this.<Integer>failWithPendingTransferRequest())
                .switchIfEmpty(Mono.defer(() ->
                        securityValidator.validateUpdateCurrentProject(auth, employeeId, newCurrentProject == null ? null : newCurrentProject.getId())
                                .onErrorResume(AccessDeniedException.class, error -> currentProjectTransferApprovalRequired(auth,
                                        employeeId, newCurrentProject == null ? null : newCurrentProject.getId(), error))
                                .flatMap(_ -> employeeRepo.findById(employeeId))
                                .switchIfEmpty(Mono.error(new BusinessError("errors.entity.not.found", Integer.toString(employeeId))))
                                .flatMap(entry -> {
                                    entry.setCurrentProjectId(newCurrentProject == null ? null : newCurrentProject.getId());
                                    entry.setCurrentProjectRole(newCurrentProject == null ? null : newCurrentProject.getRole());
                                    return doUpdate(auth.getEmployeeInfo().getEmployeeId(), now, entry, null);
                                })));
    }

    public Mono<CurrentProjectTransferRequestDto> findActiveCurrentProjectTransferRequest(AuthContext auth,
                                                                                         int employeeId) {
        // Process: the dialog needs only the current active request.
        // Permission is intentionally the same coarse transfer access check as for starting the transfer flow.
        return validateCurrentProjectTransferAccess(auth)
                .flatMap(_ -> projectTransferRequestRepo.findPendingViewByEmployeeId(employeeId))
                .flatMap(request -> canMakeProjectTransferDecision(auth, request.getEmployeeId())
                        .map(canMakeDecision -> {
                            var dto = projectTransferRequestMapper.fromView(request);
                            dto.setCanMakeDecision(canMakeDecision);
                            dto.setExpiresAt(projectTransferRequestExpiresAt(request.getCreatedAt()));
                            return dto;
                        }));
    }

    @Transactional
    public Mono<Integer> approveCurrentProjectTransferRequest(AuthContext auth,
                                                              int requestId,
                                                              @Nullable CurrentProjectTransferDecisionBody body) {
        log.info("Approve current project transfer request {} by {}", requestId, auth.getEmail());
        var now = dateTimeService.now();
        return projectTransferRequestRepo.findPendingById(requestId)
                .switchIfEmpty(BusinessErrorFactory.entityNotFound("project_transfer_request", requestId))
                .flatMap(request -> validateProjectTransferDecisionApprover(auth, request)
                        .then(applyProjectTransferRequest(auth, request, now))
                        .flatMap(historyId -> closeProjectTransferRequest(
                                request,
                                ProjectTransferRequestEntry.STATE_APPROVED,
                                decisionComment(body),
                                historyId,
                                auth,
                                now)));
    }

    @Transactional
    public Mono<Integer> rejectCurrentProjectTransferRequest(AuthContext auth,
                                                             int requestId,
                                                             @Nullable CurrentProjectTransferDecisionBody body) {
        log.info("Reject current project transfer request {} by {}", requestId, auth.getEmail());
        var now = dateTimeService.now();
        return projectTransferRequestRepo.findPendingById(requestId)
                .switchIfEmpty(BusinessErrorFactory.entityNotFound("project_transfer_request", requestId))
                .flatMap(request -> validateProjectTransferDecisionApprover(auth, request)
                        .then(closeProjectTransferRequest(
                                request,
                                ProjectTransferRequestEntry.STATE_REJECTED,
                                decisionComment(body),
                                null,
                                auth,
                                now)));
    }

    @Transactional
    public Mono<Integer> cancelCurrentProjectTransferRequest(AuthContext auth,
                                                             int requestId,
                                                             @Nullable CurrentProjectTransferDecisionBody body) {
        log.info("Cancel current project transfer request {} by {}", requestId, auth.getEmail());
        var now = dateTimeService.now();
        return projectTransferRequestRepo.findPendingById(requestId)
                .switchIfEmpty(BusinessErrorFactory.entityNotFound("project_transfer_request", requestId))
                .flatMap(request -> validateProjectTransferCancel(auth, request)
                        .then(closeProjectTransferRequest(
                                request,
                                ProjectTransferRequestEntry.STATE_CANCELED,
                                decisionComment(body),
                                null,
                                auth,
                                now)));
    }

    public Flux<CurrentProjectTransferApproverDto> findCurrentProjectTransferApprovers(AuthContext auth,
                                                                                       int employeeId,
                                                                                       int newProject) {
        // Process: approver selection is available only when direct transfer is denied
        // but the user has the target-project-side access gap required for the approval flow.
        // The returned list is based on active employee managers and then sorted/deduplicated for UI selection.
        return securityValidator.findCurrentProjectTransferAccessGap(auth, employeeId, newProject)
                .switchIfEmpty(Mono.error(new AccessDeniedException("Current project transfer approval is not required")))
                .flatMapMany(_ -> managerRepo.findActiveEmployeeManagers(employeeId, dateTimeService.now())
                        .collectList()
                        .flatMapMany(approvers -> Flux.fromIterable(currentProjectTransferApprovers(approvers))))
                .map(projectTransferRequestMapper::fromApprover);
    }

    @Transactional
    public Mono<Integer> requestCurrentProjectTransferApproval(AuthContext auth,
                                                               int employeeId,
                                                               CurrentProjectTransferApprovalRequestBody body) {
        log.info("Request current project transfer approval for employee {} by {}", employeeId, auth.getEmail());
        // Process: creation is idempotent by active pending request.
        // If another request is already pending for this employee, return the same business error used by direct update.
        // Otherwise validate the approval scenario and create a new pending request.
        return projectTransferRequestRepo.findPendingByEmployeeId(employeeId)
                .flatMap(_ -> this.<Integer>failWithPendingTransferRequest())
                .switchIfEmpty(Mono.defer(() -> createCurrentProjectTransferRequest(auth, employeeId, body)));
    }

    private Mono<Integer> createCurrentProjectTransferRequest(AuthContext auth,
                                                             int employeeId,
                                                             CurrentProjectTransferApprovalRequestBody body) {
        if (body == null || body.getFromProjectId() == null || body.getToProjectId() == null
                || body.getApproverEmployeeId() == null) {
            return Mono.error(new BusinessError("errors.current_project.transfer_request.invalid"));
        }
        // Process: do not allow creating arbitrary transfer requests.
        // The request must match the exact source/target pair from the security access-gap check,
        // and the chosen approver must be one of the active managers available for this employee.
        return securityValidator.findCurrentProjectTransferAccessGap(auth, employeeId, body.getToProjectId())
                .switchIfEmpty(Mono.error(new AccessDeniedException("Current project transfer approval is not required")))
                .flatMap(gap -> {
                    if (!body.getFromProjectId().equals(gap.fromProjectId())) {
                        return Mono.error(new BusinessError("errors.current_project.transfer_request.invalid"));
                    }
                    return validateProjectTransferApprover(employeeId, body.getApproverEmployeeId())
                            .then(saveCurrentProjectTransferRequest(auth, employeeId, body));
                });
    }

    private Mono<Void> validateProjectTransferApprover(int employeeId, int approverEmployeeId) {
        // Process: reuse the same manager source and ordering rules that feed the UI.
        // This prevents posting an approver that could not have been selected in the dialog.
        return managerRepo.findActiveEmployeeManagers(employeeId, dateTimeService.now())
                .collectList()
                .map(this::currentProjectTransferApprovers)
                .filter(approvers -> approvers.stream()
                        .anyMatch(approver -> approver.getEmployeeId() == approverEmployeeId))
                .switchIfEmpty(Mono.error(new BusinessError("errors.current_project.transfer_request.invalid_approver")))
                .then();
    }

    private Mono<Integer> saveCurrentProjectTransferRequest(AuthContext auth,
                                                           int employeeId,
                                                           CurrentProjectTransferApprovalRequestBody body) {
        var now = dateTimeService.now();
        var entry = projectTransferRequestMapper.toPendingEntry(
                body,
                employeeId,
                now,
                auth.getEmployeeInfo().getEmployeeId());
        // Process: save the request first, then write history for the persisted entity id.
        // The endpoint returns the request id, not the history row id.
        return projectTransferRequestRepo.save(entry)
                .flatMap(saved -> historyDomainService.persistHistory(
                        saved.getId(),
                        HistoryDomainService.HistoryEntityType.PROJECT_TRANSFER_REQUEST,
                        saved,
                        now,
                        auth.getEmployeeInfo().getEmployeeId())
                        .flatMap(history -> notificationOrchestrator.publishBestEffort(
                                        ProjectTransferRequestNotificationEvent.created(
                                                saved,
                                                auth.getEmployeeInfo().getEmployeeId()))
                                .thenReturn(history)))
                .map(history -> history.getEntityId());
    }

    private Mono<Void> validateProjectTransferDecisionApprover(AuthContext auth, ProjectTransferRequestEntry request) {
        return canMakeProjectTransferDecision(auth, request.getEmployeeId())
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.error(new AccessDeniedException("Only active project transfer approver can process project transfer request")))
                .then();
    }

    private Mono<Boolean> canMakeProjectTransferDecision(AuthContext auth, int employeeId) {
        // Process: approve/reject is available to the same active approver list used when the request is created.
        // The assigned approver is the primary recipient, but another current source-side approver may make the decision.
        var currentEmployeeId = auth.getEmployeeInfo().getEmployeeId();
        return managerRepo.findActiveEmployeeManagers(employeeId, dateTimeService.now())
                .collectList()
                .map(this::currentProjectTransferApprovers)
                .map(approvers -> approvers.stream()
                        .anyMatch(approver -> Objects.equals(approver.getEmployeeId(), currentEmployeeId)));
    }

    private Mono<Void> validateProjectTransferCancel(AuthContext auth, ProjectTransferRequestEntry request) {
        if (Objects.equals(request.getCreatedBy(), auth.getEmployeeInfo().getEmployeeId())
                || auth.getAuthorities().contains("update_current_project_global")) {
            return Mono.empty();
        }
        return Mono.error(new AccessDeniedException("Only request creator or global current-project manager can cancel project transfer request"));
    }

    private Mono<Integer> applyProjectTransferRequest(AuthContext auth,
                                                      ProjectTransferRequestEntry request,
                                                      OffsetDateTime now) {
        return employeeRepo.findById(request.getEmployeeId())
                .switchIfEmpty(BusinessErrorFactory.entityNotFound(EMPLOYEE_ENTITY_TYPE, request.getEmployeeId()))
                .flatMap(employee -> {
                    employee.setCurrentProjectId(request.getToProjectId());
                    employee.setCurrentProjectRole(request.getRequestedProjectRole());
                    return doUpdateAndReturnHistoryId(auth.getEmployeeInfo().getEmployeeId(), now, employee);
                });
    }

    private Mono<Integer> closeProjectTransferRequest(ProjectTransferRequestEntry request,
                                                      short state,
                                                      @Nullable String comment,
                                                      @Nullable Integer appliedEmployeeHistoryId,
                                                      AuthContext auth,
                                                      OffsetDateTime now) {
        request.setState(state);
        request.setDecisionComment(comment);
        request.setAppliedEmployeeHistoryId(appliedEmployeeHistoryId);
        request.setUpdatedAt(now);
        request.setUpdatedBy(auth.getEmployeeInfo().getEmployeeId());
        return projectTransferRequestRepo.save(request)
                .flatMap(saved -> historyDomainService.persistHistory(
                        saved.getId(),
                        HistoryDomainService.HistoryEntityType.PROJECT_TRANSFER_REQUEST,
                        saved,
                        now,
                        auth.getEmployeeInfo().getEmployeeId())
                        .flatMap(history -> notificationOrchestrator.publishBestEffort(
                                        ProjectTransferRequestNotificationEvent.closed(
                                                saved,
                                                state,
                                                auth.getEmployeeInfo().getEmployeeId()))
                                .thenReturn(history)))
                .map(history -> history.getEntityId());
    }

    private String decisionComment(@Nullable CurrentProjectTransferDecisionBody body) {
        return body == null ? null : body.getComment();
    }

    private OffsetDateTime projectTransferRequestExpiresAt(OffsetDateTime createdAt) {
        return createdAt.plus(backgroundTasksProps.getProjectTransferRequestExpiration().getMaxAge());
    }

    private <T> Mono<T> failWithPendingTransferRequest() {
        // Process: this is a backend consistency guard for stale UI/race cases.
        // The UI should refresh active transfer request details after receiving this stable error code.
        return Mono.error(new BusinessError(CURRENT_PROJECT_TRANSFER_REQUEST_ALREADY_PENDING));
    }

    private Mono<Boolean> currentProjectTransferApprovalRequired(AuthContext auth,
                                                                 int employeeId,
                                                                 Integer newProject,
                                                                 AccessDeniedException originalError) {
        // Process: this method is called after direct update was denied.
        // Keep the active-request block stronger than the approval-required fallback, then expose the approval flow
        // only when the security validator confirms the user manages target side but not source side.
        return projectTransferRequestRepo.findPendingByEmployeeId(employeeId)
                .flatMap(_ -> this.<Boolean>failWithPendingTransferRequest())
                .then(securityValidator.findCurrentProjectTransferAccessGap(auth, employeeId, newProject)
                        .flatMap(approvalRequired -> Mono.<Boolean>error(transferApprovalRequired(approvalRequired)))
                        .switchIfEmpty(Mono.error(originalError)));
    }

    private Mono<Boolean> validateCurrentProjectTransferAccess(AuthContext auth) {
        return Mono.defer(() -> {
            if (auth.getAuthorities().contains("update_current_project")
                    || auth.getAuthorities().contains("update_current_project_global")) {
                return Mono.just(true);
            }
            return Mono.error(new AccessDeniedException("Only users who can update current project can view transfer requests"));
        });
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

    private List<ManagerRecipient> currentProjectTransferApprovers(List<ManagerRecipient> recipients) {
        var result = new LinkedHashMap<Integer, ManagerRecipient>();
        recipients.stream()
                .sorted(Comparator
                        .comparingInt(this::currentProjectTransferApproverTypeOrder)
                        .thenComparing(ManagerRecipient::getDisplayName))
                .forEach(recipient -> result.putIfAbsent(recipient.getEmployeeId(), recipient));
        return new ArrayList<>(result.values());
    }

    private int currentProjectTransferApproverTypeOrder(ManagerRecipient recipient) {
        var index = CURRENT_PROJECT_TRANSFER_APPROVER_TYPES.indexOf(recipient.getManagerType());
        return index < 0 ? CURRENT_PROJECT_TRANSFER_APPROVER_TYPES.size() : index;
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

    private Mono<Integer> doUpdateAndReturnHistoryId(int currentEmployeeId, OffsetDateTime now, EmployeeWithAllDetailsEntry entry) {
        return employeeRepo.save(entry).flatMap(persisted -> {
            var history = mapper.history(persisted, currentEmployeeId, now);
            return historyRepo.save(history).map(e -> e.getId());
        });
    }

    private boolean hasAvatar(int emplId){
        return fileStorage.fileExists("avatars", emplId + ".png");
    }

}
