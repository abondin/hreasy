package ru.abondin.hreasy.platform.service.support;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.BusinessErrorFactory;
import ru.abondin.hreasy.platform.I18Helper;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.config.HrEasyCommonProps;
import ru.abondin.hreasy.platform.repo.employee.EmployeeEntry;
import ru.abondin.hreasy.platform.repo.employee.EmployeeRepo;
import ru.abondin.hreasy.platform.repo.history.HistoryEntry;
import ru.abondin.hreasy.platform.repo.suport.SupportRequestGroupRepository;
import ru.abondin.hreasy.platform.repo.suport.SupportRequestRepository;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.HistoryDomainService;
import ru.abondin.hreasy.platform.service.RateLimiter;
import ru.abondin.hreasy.platform.service.message.EmailMessageSender;
import ru.abondin.hreasy.platform.service.message.dto.HrEasyEmailMessage;
import ru.abondin.hreasy.platform.service.support.dto.NewSupportRequestDto;
import ru.abondin.hreasy.platform.service.support.dto.SupportRequestGroupDto;
import ru.abondin.hreasy.platform.service.support.dto.SupportRequestMapper;
import ru.abondin.hreasy.platform.tg.TelegramLinkNormalizer;

import java.time.OffsetDateTime;
import java.util.UUID;

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
    private final RateLimiter rateLimiter;


    @Transactional()
    public Mono<Integer> createSupportRequest(AuthContext auth, int sourceType, NewSupportRequestDto request) {
        log.info("New support request from {} to {}. Category: {}", auth.getUsername(), request.getGroup(), request.getCategory());
        var now = dateTimeService.now();
        var employeeId = auth.getEmployeeInfo().getEmployeeId();
        return rateLimiter.checkSupportRequestRateLimit(employeeId, now)
                .flatMap(v -> getGroup(request))
                .flatMap(group -> saveSupportRequest(employeeId, sourceType, now, request)
                        .flatMap(requestId -> fetchEmployeeAndSendEmail(requestId, group, request, employeeId)));
    }

    @Transactional(readOnly = true)
    public Flux<SupportRequestGroupDto> groups(AuthContext authContext) {
        log.trace("Getting support request groups by {}", authContext.getUsername());
        return groupRepository.findNotDeleted().map(mapper::fromEntry);
    }

    private Mono<Integer> saveSupportRequest(Integer employeeId, int sourceType, OffsetDateTime now, NewSupportRequestDto request) {
        var supportRequest = mapper.fromNewRequest(employeeId, sourceType, now, request);
        return requestRepository.save(supportRequest)
                .flatMap(r -> history.persistHistory(r.getId(), SUPPORT_REQUEST, r, now, employeeId))
                .map(HistoryEntry::getEntityId);
    }

    private Mono<SupportRequestGroupDto> getGroup(NewSupportRequestDto request) {
        return groupRepository.findById(request.getGroup())
                .switchIfEmpty(BusinessErrorFactory.entityNotFound("RequestGroup", request.getGroup()))
                .map(mapper::fromEntry);
    }

    private Mono<Integer> fetchEmployeeAndSendEmail(Integer requestId,
                                                    SupportRequestGroupDto group,
                                                    NewSupportRequestDto request,
                                                    Integer employeeId) {
        return employeeRepo.findById(employeeId)
                .switchIfEmpty(BusinessErrorFactory.entityNotFound("Employee", employeeId))
                .flatMap(employee -> emailMessageSender.sendMessage(buildMail(requestId, group, employee, request))
                        .map(mail -> requestId));
    }


    private HrEasyEmailMessage buildMail(int requestId,
                                         SupportRequestGroupDto group,
                                         EmployeeEntry employee,
                                         NewSupportRequestDto request) {
        if (group.getConfiguration() == null || group.getConfiguration().getEmails().isEmpty()) {
            throw new BusinessError("errors.support.request.group_cont_configured", group.getKey());
        }
        var mail = new HrEasyEmailMessage();
        mail.setTo(group.getConfiguration().getEmails());
        mail.setFrom(props.getDefaultEmailFrom());
        mail.setTitle(i18n.localize("support.request.mail.title", employee.getDisplayName()));
        mail.setClientUuid(UUID.randomUUID().toString());
        var bodyContext = new Context();
        bodyContext.setVariable("requestMessage", request.getMessage());
        bodyContext.setVariable("requestCategory", request.getCategory());
        bodyContext.setVariable("requestGroup", group.getDisplayName());
        bodyContext.setVariable("requestId", requestId);
        bodyContext.setVariable("employeeDisplayName", employee.getDisplayName());
        bodyContext.setVariable("employeeEmail", employee.getEmail());
        bodyContext.setVariable("employeeTelegram", TelegramLinkNormalizer.extractAccountName(employee.getTelegram()));
        bodyContext.setVariable("employeeTelegramLink", TelegramLinkNormalizer.normalizeTelegramLink(employee.getTelegram()));
        mail.setBody(templateEngine.process("supportrequest.html", bodyContext));
        return mail;
    }
}
