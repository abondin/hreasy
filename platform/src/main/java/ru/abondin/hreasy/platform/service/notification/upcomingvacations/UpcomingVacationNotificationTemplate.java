package ru.abondin.hreasy.platform.service.notification.upcomingvacations;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import ru.abondin.hreasy.platform.I18Helper;
import ru.abondin.hreasy.platform.config.BackgroundTasksProps;
import ru.abondin.hreasy.platform.config.HrEasyCommonProps;
import ru.abondin.hreasy.platform.service.message.dto.HrEasyEmailMessage;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

/**
 * Fill all required fields for upcoming vacation notification
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class UpcomingVacationNotificationTemplate {

    @Data
    public static class UpcomingVacationContext {
        private String clientUuid;
        private String employeeFirstname;
        private String employeeLastname;
        private String employeeEmail;
        private LocalDate startDate;
        private LocalDate endDate;
        private int daysNumber;
    }

    private final I18Helper i18n;
    private final TemplateEngine templateEngine;
    private final BackgroundTasksProps backgroundTasksProps;
    private final HrEasyCommonProps commonProps;

    @Value("classpath:mail/attachments/upcomingvacation_application.docx")
    private Resource upcomingDocument;


    @PostConstruct
    protected void postConstruct() {
        log.info("Initializing UpcomingVacationNotificationTemplate with." +
                        " Additional emails to send: {}. Default email from: {}"
                , backgroundTasksProps.getUpcomingVacation().getAdditionalEmailAddresses()
                , commonProps.getDefaultEmailFrom());
    }

    public HrEasyEmailMessage create(UpcomingVacationContext context) {
        log.info("Creating email message from {}", context);
        var uuid = UUID.randomUUID().toString();
        var message = new HrEasyEmailMessage();
        message.setTitle(i18n.localize("notification.template.upcoming-vacation.title"));
        message.setBody(processTemplate(context));
        message.setClientUuid(uuid);
        message.setAttachments(Map.of(i18n.localize("notification.template.upcoming-vacation.docname"), upcomingDocument));

        var to = new ArrayList<String>();
        // FIXME Uncomment after test period
        // to.add(context.getEmployeeEmail());
        to.addAll(backgroundTasksProps.getUpcomingVacation().getAdditionalEmailAddresses());
        message.setTo(to);
        message.setFrom(commonProps.getDefaultEmailFrom());
        return message;
    }

    private String processTemplate(UpcomingVacationContext modelContext) {
        var context = new Context();
        context.setVariable("clientUuid", modelContext.getClientUuid());
        context.setVariable("daysNumber", modelContext.getDaysNumber());
        context.setVariable("employeeEmail", modelContext.getEmployeeEmail());
        context.setVariable("employeeFirstname", modelContext.getEmployeeFirstname());
        context.setVariable("employeeLastname", modelContext.getEmployeeLastname());
        context.setVariable("endDate", modelContext.getEndDate());
        context.setVariable("startDate", modelContext.getStartDate());
        return templateEngine.process("upcomingvacation.html", context);
    }


}
