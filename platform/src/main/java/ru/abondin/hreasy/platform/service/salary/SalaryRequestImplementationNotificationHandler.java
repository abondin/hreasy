package ru.abondin.hreasy.platform.service.salary;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.repo.employee.EmployeeEntry;
import ru.abondin.hreasy.platform.repo.employee.EmployeeRepo;
import ru.abondin.hreasy.platform.service.mapper.MapperBase;
import ru.abondin.hreasy.platform.service.notification.BusinessNotificationHandler;
import ru.abondin.hreasy.platform.service.notification.NotificationPersistService;
import ru.abondin.hreasy.platform.service.notification.NotificationPlan;
import ru.abondin.hreasy.platform.service.notification.NotificationRecipient;
import ru.abondin.hreasy.platform.service.salary.dto.SalaryRequestImplementationState;

/**
 * Builds notification plans for the creator of a salary request when it is implemented or rejected.
 */
@Component
@RequiredArgsConstructor
public class SalaryRequestImplementationNotificationHandler
        implements BusinessNotificationHandler<SalaryRequestImplementationNotificationEvent> {
    private static final String IMPLEMENTED_EVENT_TYPE = "salary_request.implemented";
    private static final String REJECTED_EVENT_TYPE = "salary_request.rejected";
    private static final String IMPLEMENTED_TITLE_KEY = "notification.template.salary-request-implemented.title";
    private static final String IMPLEMENTED_BODY_KEY = "notification.template.salary-request-implemented.body";
    private static final String REJECTED_TITLE_KEY = "notification.template.salary-request-rejected.title";
    private static final String REJECTED_BODY_KEY = "notification.template.salary-request-rejected.body";
    private static final String PRIORITY = "normal";

    private final EmployeeRepo employeeRepo;

    /**
     * Returns event class handled by this strategy.
     *
     * @return salary request implementation event class
     */
    @Override
    public Class<SalaryRequestImplementationNotificationEvent> eventClass() {
        return SalaryRequestImplementationNotificationEvent.class;
    }

    /**
     * Resolves the request creator and target employee, then creates a notification plan for the creator.
     *
     * @param event salary request implementation event
     * @return notification plan for the salary request creator
     */
    @Override
    public Flux<NotificationPlan> build(SalaryRequestImplementationNotificationEvent event) {
        return Mono.zip(
                        employeeRepo.findById(event.createdByEmployeeId()),
                        employeeRepo.findById(event.employeeId())
                )
                .map(tuple -> plan(event, tuple.getT1(), tuple.getT2()))
                .flux();
    }

    private NotificationPlan plan(SalaryRequestImplementationNotificationEvent event,
                                  EmployeeEntry creator,
                                  EmployeeEntry employee) {
        var eventType = eventType(event.implementationState());
        var period = event.implementationPeriod() == null ? event.requestPeriod() : event.implementationPeriod();
        return NotificationPlan.builder()
                .eventType(eventType)
                .category(NotificationPersistService.NotificationCategory.SALARY_REQUEST.getCategory())
                .dedupeKey(dedupeKey(eventType, event.salaryRequestId(), event.createdByEmployeeId()))
                .recipient(NotificationRecipient.user(creator.getEmail(), creator.getId()))
                .priority(PRIORITY)
                .titleKey(titleKey(event.implementationState()))
                .bodyKey(bodyKey(event.implementationState()))
                .bodyArg(employee.getDisplayName())
                .bodyArg(MapperBase.formatPeriod(period))
                .bodyArg(event.rejectReason() == null ? "" : event.rejectReason())
                .context("eventType", eventType)
                .context("salaryRequestId", event.salaryRequestId())
                .context("employeeId", event.employeeId())
                .context("employeeDisplayName", employee.getDisplayName())
                .context("requestType", event.requestType())
                .context("requestPeriod", event.requestPeriod())
                .context("implementationPeriod", event.implementationPeriod())
                .context("implementationState", event.implementationState().name())
                .context("implementedByEmployeeId", event.auth().getEmployeeInfo().getEmployeeId())
                .context("rescheduledToNewPeriod", event.rescheduledToNewPeriod())
                .initiatorEmployeeId(event.auth().getEmployeeInfo().getEmployeeId())
                .build();
    }

    private String eventType(SalaryRequestImplementationState state) {
        if (state == SalaryRequestImplementationState.REJECTED) {
            return REJECTED_EVENT_TYPE;
        }
        return IMPLEMENTED_EVENT_TYPE;
    }

    private String titleKey(SalaryRequestImplementationState state) {
        if (state == SalaryRequestImplementationState.REJECTED) {
            return REJECTED_TITLE_KEY;
        }
        return IMPLEMENTED_TITLE_KEY;
    }

    private String bodyKey(SalaryRequestImplementationState state) {
        if (state == SalaryRequestImplementationState.REJECTED) {
            return REJECTED_BODY_KEY;
        }
        return IMPLEMENTED_BODY_KEY;
    }

    private String dedupeKey(String eventType, Integer salaryRequestId, Integer creatorEmployeeId) {
        return eventType + ":" + salaryRequestId + ":" + creatorEmployeeId;
    }
}
