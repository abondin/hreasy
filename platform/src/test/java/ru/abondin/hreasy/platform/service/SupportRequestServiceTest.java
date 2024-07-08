package ru.abondin.hreasy.platform.service;


import io.r2dbc.postgresql.codec.Json;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.config.HrEasyCommonProps;
import ru.abondin.hreasy.platform.repo.PostgreSQLTestContainerContextInitializer;
import ru.abondin.hreasy.platform.repo.suport.SupportRequestGroupEntry;
import ru.abondin.hreasy.platform.repo.suport.SupportRequestGroupRepository;
import ru.abondin.hreasy.platform.repo.suport.SupportRequestRepository;
import ru.abondin.hreasy.platform.service.support.SupportRequestService;
import ru.abondin.hreasy.platform.service.support.dto.NewSupportRequestDto;

import java.time.OffsetDateTime;

import static ru.abondin.hreasy.platform.service.support.dto.NewSupportRequestDto.SOURCE_TYPE_TELEGRAM;

@ActiveProfiles({"test"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ContextConfiguration(initializers = {PostgreSQLTestContainerContextInitializer.class})
@Import(TestFixedDataTimeConfig.class)
@Slf4j
class SupportRequestServiceTest extends BaseServiceTest {

    @Autowired
    private SupportRequestService service;

    @Autowired
    private TestFixedDataTimeConfig.TestFixedDataTimeService dateTimeService;

    @Autowired
    private SupportRequestRepository requestRepository;
    @Autowired
    private SupportRequestGroupRepository groupRepository;
    @Autowired
    private RateLimiter rateLimiter;

    @Autowired
    private HrEasyCommonProps props;

    @Autowired
    private DatabaseClient db;

    private String defaultGroup;

    @BeforeEach
    protected void beforeEach() {
        rateLimiter.reset();
        cleanTables().block(MONO_DEFAULT_TIMEOUT);
        initEmployeesDataAndLogin();
        defaultGroup = defaultSupportGroup().block(MONO_DEFAULT_TIMEOUT);
    }

    @Test
    void testSendRequest() {
        var request = new NewSupportRequestDto();
        request.setGroup(defaultGroup);
        request.setMessage("Hello world");
        request.setCategory("Licensing and activation issues");
        var test = service.createSupportRequest(auth, 1, request);
        StepVerifier
                .create(test).expectNextCount(1).verifyComplete();
        var checkNothingCreatedIn = requestRepository.findAll();
        StepVerifier.create(checkNothingCreatedIn)
                .expectNextMatches(e -> {
                    try {
                        Assertions.assertEquals("Licensing and activation issues", e.getCategory());
                        Assertions.assertEquals("Hello world", e.getMessage());
                        Assertions.assertEquals(defaultGroup, e.getSupportGroup());
                        Assertions.assertEquals(auth.getEmployeeInfo().getEmployeeId(), e.getEmployeeId().intValue());
                    } catch (AssertionError ex) {
                        log.error("Assertion error", ex);
                        return false;
                    }
                    return true;
                }).verifyComplete();
    }

    @Test
    void testRateLimit() {
        int allowedRequestCnt = props.getMaxSupportRequestsInHour();
        Mono<?> test = null;
        dateTimeService.init(OffsetDateTime.now().minusHours(2));
        for (int i = 1; i <= allowedRequestCnt * 2; i++) {
            var request = new NewSupportRequestDto();
            request.setGroup(defaultGroup);
            request.setMessage("Rate limit stress: " + i);
            var mono = service.createSupportRequest(auth, SOURCE_TYPE_TELEGRAM, request).doOnNext((a) -> {
                dateTimeService.init(dateTimeService.now().plusMinutes(5));
            });
            test = test == null ? mono : test.then(mono);
        }
        StepVerifier
                .create(test)
                .expectErrorMatches(e -> e instanceof BusinessError &&
                        ((BusinessError) e).getCode().equals("errors.support.request.rate_limit_exceeded"))
                .verify(StepVerifier.DEFAULT_VERIFY_TIMEOUT);
        // Verify only 5 requests created
        StepVerifier.create(requestRepository.findAll())
                .expectNextCount(allowedRequestCnt).verifyComplete();
        // Check that employee can send requests again after some delay
        dateTimeService.init(dateTimeService.now().plusMinutes(40));
        var request = new NewSupportRequestDto();
        request.setGroup(defaultGroup);
        request.setMessage("Rate limit stress after delay ");
        test = service.createSupportRequest(auth, SOURCE_TYPE_TELEGRAM, request);
        StepVerifier
                .create(test)
                .expectNextCount(1)
                .verifyComplete();
        // Verify that 6 requests now
        StepVerifier.create(requestRepository.findAll())
                .expectNextCount(allowedRequestCnt+1).verifyComplete();
    }


    @Test
    void testSendRequestGroupNotExists() {
        var request = new NewSupportRequestDto();
        request.setGroup("test_group_not_exists");
        request.setMessage("Hello world");
        var test = service.createSupportRequest(auth, SOURCE_TYPE_TELEGRAM, request);
        StepVerifier
                .create(test)
                .expectErrorMatches(e -> e instanceof BusinessError &&
                        ((BusinessError) e).getCode().equals("errors.entity_of_type.not.found"))
                .verify(StepVerifier.DEFAULT_VERIFY_TIMEOUT);
        var checkNothingCreatedIn = requestRepository.findAll();
        StepVerifier.create(checkNothingCreatedIn)
                .expectNextCount(0).verifyComplete();
    }


    private Mono<Void> cleanTables() {
        return db.sql("delete from support.support_request").then()
                .then(db.sql("delete from support.support_request_group").then());
    }

    private Mono<String> defaultSupportGroup() {
        var group = new SupportRequestGroupEntry();
        group.setConfiguration(Json.of("{\"emails\":[\"test@company.org\"]}"));
        group.setKey("test_group");
        group.setDisplayName("Test Support Request Group");
        group.setCreatedAt(dateTimeService.now());
        group.setCreatedBy(auth.getEmployeeInfo().getEmployeeId());
        return groupRepository.save(group).map(SupportRequestGroupEntry::getKey);
    }

}
