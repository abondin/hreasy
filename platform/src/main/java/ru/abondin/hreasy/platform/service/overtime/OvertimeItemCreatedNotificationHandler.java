package ru.abondin.hreasy.platform.service.overtime;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import ru.abondin.hreasy.platform.repo.employee.EmployeeRepo;
import ru.abondin.hreasy.platform.repo.manager.ManagerRecipient;
import ru.abondin.hreasy.platform.repo.manager.ManagerRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.notification.BusinessNotificationHandler;
import ru.abondin.hreasy.platform.service.notification.NotificationPersistService;
import ru.abondin.hreasy.platform.service.notification.NotificationPlan;
import ru.abondin.hreasy.platform.service.notification.NotificationRecipient;

/**
 * Builds notification plans for managers when an employee adds an overtime item.
 */
@Component
@RequiredArgsConstructor
public class OvertimeItemCreatedNotificationHandler
        implements BusinessNotificationHandler<OvertimeItemCreatedNotificationEvent> {
    private static final String EVENT_TYPE = "overtime.item_created";
    private static final String TITLE_KEY = "notification.template.overtime-item-created.title";
    private static final String BODY_KEY = "notification.template.overtime-item-created.body";
    private static final String PRIORITY = "normal";
    private static final String MANAGER_PERMISSION = "overtime_view";

    private final ManagerRepo managerRepo;
    private final EmployeeRepo employeeRepo;
    private final DateTimeService dateTimeService;

    /**
     * Returns event class handled by this strategy.
     *
     * @return overtime item creation event class
     */
    @Override
    public Class<OvertimeItemCreatedNotificationEvent> eventClass() {
        return OvertimeItemCreatedNotificationEvent.class;
    }

    /**
     * Resolves active managers with {@code overtime_view} and creates one notification plan for each manager.
     *
     * @param event overtime item creation event
     * @return notification plans for eligible managers
     */
    @Override
    public Flux<NotificationPlan> build(OvertimeItemCreatedNotificationEvent event) {
        var employeeId = event.employeeId();

        return employeeRepo.findById(employeeId)
                .flatMapMany(employee -> managerRepo.findActiveEmployeeManagersWithPermission(
                                employeeId, MANAGER_PERMISSION, dateTimeService.now())
                        .map(manager -> plan(event, manager, employee.getDisplayName())));
    }

    private NotificationPlan plan(OvertimeItemCreatedNotificationEvent event,
                                  ManagerRecipient manager,
                                  String employeeDisplayName) {
        return NotificationPlan.builder()
                .eventType(EVENT_TYPE)
                .category(NotificationPersistService.NotificationCategory.OVERTIME.getCategory())
                .dedupeKey(dedupeKey(event.reportId(), event.itemId(), manager.getEmployeeId()))
                .recipient(NotificationRecipient.user(manager.getEmail(), manager.getEmployeeId()))
                .priority(PRIORITY)
                .titleKey(TITLE_KEY)
                .bodyKey(BODY_KEY)
                .bodyArg(employeeDisplayName)
                .bodyArg(event.period())
                .bodyArg(event.itemDate())
                .bodyArg(event.hours())
                .context("eventType", EVENT_TYPE)
                .context("employeeId", event.employeeId())
                .context("employeeDisplayName", employeeDisplayName)
                .context("overtimeReportId", event.reportId())
                .context("overtimeItemId", event.itemId())
                .context("period", event.period())
                .context("itemDate", event.itemDate().toString())
                .context("hours", event.hours())
                .initiatorEmployeeId(event.auth().getEmployeeInfo().getEmployeeId())
                .build();
    }

    private String dedupeKey(Integer reportId, Integer itemId, Integer managerEmployeeId) {
        return EVENT_TYPE + ":" + reportId + ":" + itemId + ":" + managerEmployeeId;
    }
}
