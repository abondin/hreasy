package ru.abondin.hreasy.platform.service.overtime;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import ru.abondin.hreasy.platform.repo.employee.EmployeeRepo;
import ru.abondin.hreasy.platform.repo.manager.ManagerRecipient;
import ru.abondin.hreasy.platform.repo.manager.ManagerRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.mapper.MapperBase;
import ru.abondin.hreasy.platform.service.notification.BusinessNotificationHandler;
import ru.abondin.hreasy.platform.service.notification.NotificationPersistService;
import ru.abondin.hreasy.platform.service.notification.NotificationPlan;
import ru.abondin.hreasy.platform.service.notification.NotificationRecipient;

/**
 * Builds notification plans for managers when an employee deletes an overtime item.
 */
@Component
@RequiredArgsConstructor
public class OvertimeItemDeletedNotificationHandler
        implements BusinessNotificationHandler<OvertimeItemDeletedNotificationEvent> {
    private static final String EVENT_TYPE = "overtime.item_deleted";
    private static final String TITLE_KEY = "notification.template.overtime-item-deleted.title";
    private static final String BODY_KEY = "notification.template.overtime-item-deleted.body";
    private static final String PRIORITY = "normal";
    private static final String MANAGER_PERMISSION = "overtime_view";

    private final ManagerRepo managerRepo;
    private final EmployeeRepo employeeRepo;
    private final DateTimeService dateTimeService;

    /**
     * Returns event class handled by this strategy.
     *
     * @return overtime item deletion event class
     */
    @Override
    public Class<OvertimeItemDeletedNotificationEvent> eventClass() {
        return OvertimeItemDeletedNotificationEvent.class;
    }

    /**
     * Resolves active managers with {@code overtime_view} and creates one notification plan for each manager.
     *
     * @param event overtime item deletion event
     * @return notification plans for eligible managers
     */
    @Override
    public Flux<NotificationPlan> build(OvertimeItemDeletedNotificationEvent event) {
        var employeeId = event.employeeId();

        return employeeRepo.findById(employeeId)
                .flatMapMany(employee -> managerRepo.findActiveEmployeeManagersWithPermission(
                                employeeId, MANAGER_PERMISSION, dateTimeService.now())
                        .map(manager -> plan(event, manager, employee.getDisplayName())));
    }

    private NotificationPlan plan(OvertimeItemDeletedNotificationEvent event,
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
                .bodyArg(MapperBase.formatPeriod(event.period()))
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
