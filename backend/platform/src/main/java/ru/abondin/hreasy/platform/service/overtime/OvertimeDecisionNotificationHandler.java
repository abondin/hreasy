package ru.abondin.hreasy.platform.service.overtime;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import ru.abondin.hreasy.platform.repo.employee.EmployeeRepo;
import ru.abondin.hreasy.platform.service.mapper.MapperBase;
import ru.abondin.hreasy.platform.service.notification.BusinessNotificationHandler;
import ru.abondin.hreasy.platform.service.notification.NotificationPersistService;
import ru.abondin.hreasy.platform.service.notification.NotificationPlan;
import ru.abondin.hreasy.platform.service.notification.NotificationRecipient;
import ru.abondin.hreasy.platform.service.overtime.dto.OvertimeApprovalDecisionDto;

/**
 * Builds notification plans for an employee when an overtime report is approved or declined.
 */
@Component
@RequiredArgsConstructor
public class OvertimeDecisionNotificationHandler
        implements BusinessNotificationHandler<OvertimeDecisionNotificationEvent> {
    private static final String APPROVED_EVENT_TYPE = "overtime.approved";
    private static final String DECLINED_EVENT_TYPE = "overtime.declined";
    private static final String APPROVED_TITLE_KEY = "notification.template.overtime-approved.title";
    private static final String APPROVED_BODY_KEY = "notification.template.overtime-approved.body";
    private static final String DECLINED_TITLE_KEY = "notification.template.overtime-declined.title";
    private static final String DECLINED_BODY_KEY = "notification.template.overtime-declined.body";
    private static final String PRIORITY = "normal";

    private final EmployeeRepo employeeRepo;

    /**
     * Returns event class handled by this strategy.
     *
     * @return overtime decision event class
     */
    @Override
    public Class<OvertimeDecisionNotificationEvent> eventClass() {
        return OvertimeDecisionNotificationEvent.class;
    }

    /**
     * Resolves the report employee and creates a notification plan for the saved decision.
     *
     * @param event overtime decision event
     * @return notification plan for the report employee
     */
    @Override
    public Flux<NotificationPlan> build(OvertimeDecisionNotificationEvent event) {
        return employeeRepo.findById(event.employeeId())
                .flux()
                .map(employee -> plan(event, employee.getEmail()));
    }

    private NotificationPlan plan(OvertimeDecisionNotificationEvent event, String employeeEmail) {
        var eventType = eventType(event);
        return NotificationPlan.builder()
                .eventType(eventType)
                .category(NotificationPersistService.NotificationCategory.OVERTIME.getCategory())
                .dedupeKey(dedupeKey(eventType, event.reportId(), event.employeeId(), event.decisionId()))
                .recipient(NotificationRecipient.user(employeeEmail, event.employeeId()))
                .priority(PRIORITY)
                .titleKey(titleKey(event))
                .bodyKey(bodyKey(event))
                .bodyArg(MapperBase.formatPeriod(event.period()))
                .bodyArg(event.comment() == null ? "" : event.comment())
                .context("eventType", eventType)
                .context("employeeId", event.employeeId())
                .context("overtimeReportId", event.reportId())
                .context("period", event.period())
                .context("decisionId", event.decisionId())
                .context("decision", event.decision().name())
                .context("approverEmployeeId", event.approverEmployeeId())
                .initiatorEmployeeId(event.auth().getEmployeeInfo().getEmployeeId())
                .build();
    }

    private String eventType(OvertimeDecisionNotificationEvent event) {
        if (event.decision() == OvertimeApprovalDecisionDto.ApprovalDecision.DECLINED) {
            return DECLINED_EVENT_TYPE;
        }
        return APPROVED_EVENT_TYPE;
    }

    private String titleKey(OvertimeDecisionNotificationEvent event) {
        if (event.decision() == OvertimeApprovalDecisionDto.ApprovalDecision.DECLINED) {
            return DECLINED_TITLE_KEY;
        }
        return APPROVED_TITLE_KEY;
    }

    private String bodyKey(OvertimeDecisionNotificationEvent event) {
        if (event.decision() == OvertimeApprovalDecisionDto.ApprovalDecision.DECLINED) {
            return DECLINED_BODY_KEY;
        }
        return APPROVED_BODY_KEY;
    }

    private String dedupeKey(String eventType, Integer reportId, int employeeId, Integer decisionId) {
        return eventType + ":" + reportId + ":" + employeeId + ":" + decisionId;
    }
}
