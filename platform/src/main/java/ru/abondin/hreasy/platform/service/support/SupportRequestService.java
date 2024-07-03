package ru.abondin.hreasy.platform.service.support;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.BusinessErrorFactory;
import ru.abondin.hreasy.platform.I18Helper;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.config.HrEasyCommonProps;
import ru.abondin.hreasy.platform.repo.employee.EmployeeEntry;
import ru.abondin.hreasy.platform.repo.employee.EmployeeRepo;
import ru.abondin.hreasy.platform.repo.history.HistoryEntry;
import ru.abondin.hreasy.platform.repo.suport.SupportRequestGroupEntry;
import ru.abondin.hreasy.platform.repo.suport.SupportRequestGroupRepository;
import ru.abondin.hreasy.platform.repo.suport.SupportRequestRepository;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.HistoryDomainService;
import ru.abondin.hreasy.platform.service.message.EmailMessageSender;
import ru.abondin.hreasy.platform.service.message.dto.HrEasyEmailMessage;
import ru.abondin.hreasy.platform.service.support.dto.NewSupportRequestDto;
import ru.abondin.hreasy.platform.service.support.dto.SupportGroupConfiguration;
import ru.abondin.hreasy.platform.service.support.dto.SupportRequestMapper;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static ru.abondin.hreasy.platform.service.HistoryDomainService.HistoryEntityType.SUPPORT_REQUEST;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupportRequestService {
    private final SupportRequestGroupRepository groupRepository;
    private final SupportRequestRepository requestRepository;
    private final DateTimeService dateTimeService;
    private final EmailMessageSender emailMessageSender;
    private final SupportRequestMapper mapper;
    private final HistoryDomainService history;
    private final TemplateEngine templateEngine;
    private final HrEasyCommonProps props;
    private final I18Helper i18n;
    private final EmployeeRepo employeeRepo;

    private final ConcurrentHashMap<Integer, ConcurrentLinkedQueue<OffsetDateTime>> requestTimestampsRateLimiterCache = new ConcurrentHashMap<>();

    @Transactional
    public Mono<Integer> createSupportRequest(AuthContext auth, int sourceType, NewSupportRequestDto request) {
        log.info("New support request from {} to {} group", auth.getUsername(), request.getGroup());
        var now = dateTimeService.now();
        var employeeId = auth.getEmployeeInfo().getEmployeeId();

        // Step 0: Everyone can request support, no additional permission validation required
        return checkRequestRateLimit(employeeId, now)
                .flatMap(v -> saveSupportRequest(employeeId, sourceType, now, request))
                .flatMap(requestId -> processSupportRequest(requestId, auth, request, employeeId));
    }


    private Mono<Boolean> checkRequestRateLimit(Integer employeeId, OffsetDateTime now) {
        var timestamps = requestTimestampsRateLimiterCache.computeIfAbsent(employeeId, k -> new ConcurrentLinkedQueue<>());
        synchronized (timestamps) {
            // Remove timestamps older than 1 hour
            while (!timestamps.isEmpty() && ChronoUnit.HOURS.between(timestamps.peek(), now) >= 1) {
                timestamps.poll();
            }

            if (timestamps.size() >= props.getMaxSupportRequestsInHour()) {
                return Mono.error(new BusinessError("errors.support.request.rate_limit_exceeded", Integer.toString(props.getMaxSupportRequestsInHour())));
            }

            timestamps.add(now);
        }

        return Mono.just(true);
    }


    private Mono<Integer> saveSupportRequest(Integer employeeId, int sourceType, OffsetDateTime now, NewSupportRequestDto request) {
        var supportRequest = mapper.fromNewRequest(employeeId, sourceType, now, request);
        return requestRepository.save(supportRequest)
                .flatMap(r -> history.persistHistory(r.getId(), SUPPORT_REQUEST, r, now, employeeId))
                .map(HistoryEntry::getEntityId);
    }

    private Mono<Integer> processSupportRequest(Integer requestId, AuthContext auth, NewSupportRequestDto request, Integer employeeId) {
        return groupRepository.findById(request.getGroup())
                .switchIfEmpty(BusinessErrorFactory.entityNotFound("RequestGroup", request.getGroup()))
                .flatMap(group -> validateGroupConfiguration(group, request.getGroup()))
                .flatMap(configuration -> fetchEmployeeAndSendEmail(auth, requestId, configuration, request, employeeId));
    }

    private Mono<SupportGroupConfiguration> validateGroupConfiguration(SupportRequestGroupEntry group, String groupId) {
        var configuration = mapper.groupConfiguration(group.getConfiguration());
        if (configuration == null || configuration.getEmails().isEmpty()) {
            return Mono.error(new BusinessError("errors.support.request.group_not_configured", groupId));
        }
        return Mono.just(configuration);
    }

    private Mono<Integer> fetchEmployeeAndSendEmail(AuthContext auth, Integer requestId, SupportGroupConfiguration configuration, NewSupportRequestDto request, Integer employeeId) {
        return employeeRepo.findById(employeeId)
                .switchIfEmpty(BusinessErrorFactory.entityNotFound("Employee", employeeId))
                .flatMap(employee -> emailMessageSender.sendMessage(buildMail(auth, requestId, configuration, employee, request))
                        .map(mail -> requestId));
    }


    private HrEasyEmailMessage buildMail(AuthContext auth,
                                         int requestId,
                                         SupportGroupConfiguration configuration,
                                         EmployeeEntry employee,
                                         NewSupportRequestDto request) {
        var mail = new HrEasyEmailMessage();
        mail.setTo(configuration.getEmails());
        mail.setFrom(props.getDefaultEmailFrom());
        mail.setTitle(i18n.localize("support.request.mail.title", employee.getDisplayName()));
        mail.setClientUuid(UUID.randomUUID().toString());
        var bodyContext = new Context();
        bodyContext.setVariable("request", request);
        bodyContext.setVariable("requestId", requestId);
        bodyContext.setVariable("employee", employee);
        mail.setBody(templateEngine.process("supportrequest.html", bodyContext));
        return mail;
    }
}
