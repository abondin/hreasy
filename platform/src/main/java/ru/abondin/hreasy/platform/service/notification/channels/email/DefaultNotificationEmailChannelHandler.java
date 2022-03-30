package ru.abondin.hreasy.platform.service.notification.channels.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.core.io.Resource;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import ru.abondin.hreasy.platform.I18Helper;
import ru.abondin.hreasy.platform.config.BackgroundTasksProps;
import ru.abondin.hreasy.platform.config.HrEasyCommonProps;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.notification.channels.NotificationRoute;
import ru.abondin.hreasy.platform.service.notification.dto.NewNotificationDto;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Send email using smtp
 */
@Service
@ConditionalOnExpression("!T(org.springframework.util.StringUtils).isEmpty('${spring.mail.host:}')")
@RequiredArgsConstructor
@Slf4j
public class DefaultNotificationEmailChannelHandler implements NotificationEmailChannelHandler {

    private final JavaMailSender emailSender;
    private final HrEasyCommonProps commonProps;
    private final DateTimeService dateTimeService;
    private final BackgroundTasksProps tasksProps;
    private final I18Helper i18Helper;


    @Value("classpath:mail/attachments/upcoming_application.docx")
    private Resource upcomingApplication;

    private final Map<String, Function<NewNotificationDto, List<MimeMessage>>> templateHandlers = Map
            .of(MAIL_CATEGORY_UPCOMING_VACATION, (notification -> prepareUpcomingVacation(notification)));


    @Override
    public Flux<NotificationEmailHandleResult> handleNotification(NewNotificationDto newNotificationDto, NotificationRoute route) {

        Function<NewNotificationDto, List<MimeMessage>> messageGenerator = null;

        // 1. Check template for category
        if (Strings.isNotBlank(newNotificationDto.getCategory())) {
            messageGenerator = templateHandlers.get(newNotificationDto.getCategory());
        }
        if (messageGenerator == null) {
            //TODO Implement generic email message generation from NewNotificationDto
            throw new UnsupportedOperationException("Only templated emails are supported at this moment");
        }

        return Flux.fromStream(messageGenerator.apply(newNotificationDto).stream())
                .map(message -> {
                    emailSender.send(message);
                    return NotificationEmailHandleResult.builder()
                            .email("//TODO")
                            .handledAt(dateTimeService.now())
                            .notificationClientId(newNotificationDto.getClientUuid())
                            .build();
                });
    }

    private List<MimeMessage> prepareUpcomingVacation(NewNotificationDto notification) {
        try {
            var message = emailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, true, "UTF-8");
            for (var address : tasksProps.getUpcomingVacation().getAdditionalEmailAddresses()) {
                helper.addTo(address);
            }
            helper.setFrom(commonProps.getDefaultEmailFrom());
            helper.setSubject(i18Helper.localize("upcoming_vacation_subject"));
            helper.setText("Hello world from HR Easy");
        } catch (MessagingException ex) {
            throw new MailPreparationException(ex);
        }
    }
}
