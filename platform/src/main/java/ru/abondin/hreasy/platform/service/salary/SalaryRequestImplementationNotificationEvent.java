package ru.abondin.hreasy.platform.service.salary;

import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.service.notification.BusinessNotificationEvent;
import ru.abondin.hreasy.platform.service.salary.dto.SalaryRequestImplementationState;

/**
 * Event emitted after a salary request implementation state has been saved.
 *
 * @param auth action initiator
 * @param salaryRequestId salary request id
 * @param employeeId target employee from the salary request
 * @param createdByEmployeeId employee who created the salary request and can view it by ownership rule
 * @param requestType salary request type code
 * @param requestPeriod requested salary request period
 * @param implementationPeriod actual implementation period, if available
 * @param implementationState saved implementation state
 * @param rejectReason reject reason, if the request was rejected
 * @param rescheduledToNewPeriod optional period used when rejection also reschedules the request
 */
public record SalaryRequestImplementationNotificationEvent(
        AuthContext auth,
        Integer salaryRequestId,
        Integer employeeId,
        Integer createdByEmployeeId,
        Short requestType,
        Integer requestPeriod,
        Integer implementationPeriod,
        SalaryRequestImplementationState implementationState,
        String rejectReason,
        Integer rescheduledToNewPeriod
) implements BusinessNotificationEvent {
}
