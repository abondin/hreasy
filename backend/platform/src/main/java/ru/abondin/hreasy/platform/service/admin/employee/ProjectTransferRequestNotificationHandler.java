package ru.abondin.hreasy.platform.service.admin.employee;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.config.BackgroundTasksProps;
import ru.abondin.hreasy.platform.repo.dict.DictProjectEntry;
import ru.abondin.hreasy.platform.repo.dict.DictProjectRepo;
import ru.abondin.hreasy.platform.repo.employee.EmployeeEntry;
import ru.abondin.hreasy.platform.repo.employee.EmployeeRepo;
import ru.abondin.hreasy.platform.service.WebUiLinkBuilder;
import ru.abondin.hreasy.platform.service.notification.BusinessNotificationHandler;
import ru.abondin.hreasy.platform.service.notification.NotificationPersistService;
import ru.abondin.hreasy.platform.service.notification.NotificationPlan;
import ru.abondin.hreasy.platform.service.notification.NotificationRecipient;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

/**
 * Builds notification plans for project transfer request participants.
 */
@Component
@RequiredArgsConstructor
public class ProjectTransferRequestNotificationHandler
        implements BusinessNotificationHandler<ProjectTransferRequestNotificationEvent> {
    private static final String EVENT_CREATED = "project_transfer.request_created";
    private static final String EVENT_APPROVED = "project_transfer.request_approved";
    private static final String EVENT_REJECTED = "project_transfer.request_rejected";
    private static final String EVENT_CANCELED = "project_transfer.request_canceled";
    private static final String EVENT_EXPIRED = "project_transfer.request_expired";
    private static final String ACTION_TITLE_KEY = "notification.template.project-transfer-request.action";
    private static final String PRIORITY = "normal";

    private final EmployeeRepo employeeRepo;
    private final DictProjectRepo projectRepo;
    private final BackgroundTasksProps backgroundTasksProps;
    private final WebUiLinkBuilder webUiLinkBuilder;

    @Override
    public Class<ProjectTransferRequestNotificationEvent> eventClass() {
        return ProjectTransferRequestNotificationEvent.class;
    }

    @Override
    public Flux<NotificationPlan> build(ProjectTransferRequestNotificationEvent event) {
        return Mono.zip(
                        employeeRepo.findById(event.employeeId()),
                        employeeRepo.findById(event.createdByEmployeeId()),
                        employeeRepo.findById(event.approverEmployeeId()),
                        projectRepo.findById(event.fromProjectId()),
                        projectRepo.findById(event.toProjectId()))
                .flatMapMany(tuple -> Flux.fromIterable(plans(
                        event,
                        tuple.getT1(),
                        tuple.getT2(),
                        tuple.getT3(),
                        tuple.getT4(),
                        tuple.getT5())));
    }

    private List<NotificationPlan> plans(ProjectTransferRequestNotificationEvent event,
                                         EmployeeEntry employee,
                                         EmployeeEntry creator,
                                         EmployeeEntry approver,
                                         DictProjectEntry fromProject,
                                         DictProjectEntry toProject) {
        var recipients = recipients(event, creator, approver);
        var plans = new ArrayList<NotificationPlan>();
        recipients.forEach((_, recipient) -> plans.add(plan(event, employee, recipient, fromProject, toProject)));
        return plans;
    }

    private LinkedHashMap<Integer, EmployeeEntry> recipients(ProjectTransferRequestNotificationEvent event,
                                                             EmployeeEntry creator,
                                                             EmployeeEntry approver) {
        var recipients = new LinkedHashMap<Integer, EmployeeEntry>();
        switch (event.kind()) {
            case CREATED -> recipients.put(approver.getId(), approver);
            case APPROVED, REJECTED -> {
                recipients.put(creator.getId(), creator);
                if (!Objects.equals(event.actionEmployeeId(), approver.getId())) {
                    recipients.put(approver.getId(), approver);
                }
            }
            case CANCELED -> {
                recipients.put(approver.getId(), approver);
                if (!Objects.equals(event.actionEmployeeId(), creator.getId())) {
                    recipients.put(creator.getId(), creator);
                }
            }
            case EXPIRED -> {
                recipients.put(creator.getId(), creator);
                recipients.put(approver.getId(), approver);
            }
        }
        return recipients;
    }

    private NotificationPlan plan(ProjectTransferRequestNotificationEvent event,
                                  EmployeeEntry employee,
                                  EmployeeEntry recipient,
                                  DictProjectEntry fromProject,
                                  DictProjectEntry toProject) {
        var eventType = eventType(event.kind());
        var expiresAt = expiresAt(event);
        return NotificationPlan.builder()
                .eventType(eventType)
                .category(NotificationPersistService.NotificationCategory.PROJECT_TRANSFER.getCategory())
                .dedupeKey(dedupeKey(eventType, event.requestId(), recipient.getId()))
                .recipient(NotificationRecipient.user(recipient.getEmail(), recipient.getId()))
                .priority(PRIORITY)
                .titleKey(titleKey(event.kind()))
                .bodyKey(bodyKey(event.kind()))
                .bodyArg(employee.getDisplayName())
                .bodyArg(fromProject.getName())
                .bodyArg(toProject.getName())
                .bodyArg(event.comment() == null ? "" : event.comment())
                .bodyArg(expiresAt)
                .actionTitleKey(ACTION_TITLE_KEY)
                .actionUrl(webUiLinkBuilder.employeeProjectChangeDialog(event.employeeId()))
                .context("eventType", eventType)
                .context("projectTransferRequestId", event.requestId())
                .context("employeeId", event.employeeId())
                .context("employeeDisplayName", employee.getDisplayName())
                .context("fromProjectId", event.fromProjectId())
                .context("fromProjectName", fromProject.getName())
                .context("toProjectId", event.toProjectId())
                .context("toProjectName", toProject.getName())
                .context("state", event.kind().name())
                .context("createdByEmployeeId", event.createdByEmployeeId())
                .context("approverEmployeeId", event.approverEmployeeId())
                .context("createdAt", event.createdAt())
                .context("expiresAt", expiresAt)
                .context("actionEmployeeId", event.actionEmployeeId())
                .initiatorEmployeeId(event.actionEmployeeId())
                .build();
    }

    private String eventType(ProjectTransferRequestNotificationEvent.Kind kind) {
        return switch (kind) {
            case CREATED -> EVENT_CREATED;
            case APPROVED -> EVENT_APPROVED;
            case REJECTED -> EVENT_REJECTED;
            case CANCELED -> EVENT_CANCELED;
            case EXPIRED -> EVENT_EXPIRED;
        };
    }

    private String titleKey(ProjectTransferRequestNotificationEvent.Kind kind) {
        return switch (kind) {
            case CREATED -> "notification.template.project-transfer-request-created.title";
            case APPROVED -> "notification.template.project-transfer-request-approved.title";
            case REJECTED -> "notification.template.project-transfer-request-rejected.title";
            case CANCELED -> "notification.template.project-transfer-request-canceled.title";
            case EXPIRED -> "notification.template.project-transfer-request-expired.title";
        };
    }

    private String bodyKey(ProjectTransferRequestNotificationEvent.Kind kind) {
        return switch (kind) {
            case CREATED -> "notification.template.project-transfer-request-created.body";
            case APPROVED -> "notification.template.project-transfer-request-approved.body";
            case REJECTED -> "notification.template.project-transfer-request-rejected.body";
            case CANCELED -> "notification.template.project-transfer-request-canceled.body";
            case EXPIRED -> "notification.template.project-transfer-request-expired.body";
        };
    }

    private String dedupeKey(String eventType, Integer requestId, Integer recipientEmployeeId) {
        return eventType + ":" + requestId + ":" + recipientEmployeeId;
    }

    private OffsetDateTime expiresAt(ProjectTransferRequestNotificationEvent event) {
        return event.createdAt().plus(backgroundTasksProps.getProjectTransferRequestExpiration().getMaxAge());
    }
}
