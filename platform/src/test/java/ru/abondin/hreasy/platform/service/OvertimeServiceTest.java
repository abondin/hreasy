package ru.abondin.hreasy.platform.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.abondin.hreasy.platform.TestDataContainer;
import ru.abondin.hreasy.platform.TestEmployees;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.config.HrEasySecurityProps;
import ru.abondin.hreasy.platform.repo.SqlServerContextInitializer;
import ru.abondin.hreasy.platform.service.overtime.OvertimeService;
import ru.abondin.hreasy.platform.service.overtime.dto.NewOvertimeItemDto;

import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;

@Disabled("Because of weird java.lang.IllegalStateException: No MssqlRowMetadata available")
@ActiveProfiles({"test", "dev"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ContextConfiguration(initializers = {SqlServerContextInitializer.class})
@Slf4j
public class OvertimeServiceTest {

    private final static Duration MONO_DEFAULT_TIMEOUT = Duration.ofSeconds(3);


    @Autowired
    private OvertimeService overtimeService;

    @Autowired
    private AuthHandler authHandler;

    @Autowired
    private TestDataContainer testData;

    @Autowired
    private HrEasySecurityProps securityProps;

    @BeforeEach
    protected void validateTestConfiguration() {
        if (securityProps.getMasterPassword().isBlank()) {
            Assertions.fail("No master password found");
        }
        testData.initAsync().block(MONO_DEFAULT_TIMEOUT);
    }

    @Test
    public void testGetOvertimeNoPermissions() {
        var jensonId = testData.employees.get(TestEmployees.FMS_Empl_Jenson_Curtis);
        var ctx = auth(TestEmployees.FMS_Empl_Ammara_Knott).block(MONO_DEFAULT_TIMEOUT);
        StepVerifier
                .create(overtimeService.getOrStub(jensonId, 202008, ctx))
                .expectError(AccessDeniedException.class).verify(MONO_DEFAULT_TIMEOUT);
    }

    @Test
    public void testGetOvertimeSummary() {
        var ctx = auth(TestEmployees.FMS_Manager_Jawad_Mcghee).block(MONO_DEFAULT_TIMEOUT);
        StepVerifier
                .create(
                        overtimeService.addItem(
                                ctx.getEmployeeInfo().getEmployeeId(),
                                202008,
                                new NewOvertimeItemDto(LocalDate.now(), testData.projects.get("M1 Billing"), 1, "testGetOvertimeSummary"),
                                ctx).thenMany(overtimeService.getSummary(202008, ctx)))
                //TODO Validate the actual result
                .expectNextMatches(o -> true).verifyComplete();
    }

    @Test
    public void testGetOvertimeSummaryNoPermissions() {
        var ctx = auth(TestEmployees.FMS_Empl_Ammara_Knott).block(MONO_DEFAULT_TIMEOUT);
        StepVerifier
                .create(overtimeService.getSummary(202008, ctx))
                .expectError(AccessDeniedException.class).verify(MONO_DEFAULT_TIMEOUT);
    }

    @Test
    public void testReportMyOvertime() {
        var jensonId = testData.employees.get(TestEmployees.FMS_Empl_Jenson_Curtis);
        var ctx = auth(TestEmployees.FMS_Empl_Jenson_Curtis).block(MONO_DEFAULT_TIMEOUT);
        StepVerifier
                .create(
                        overtimeService.addItem(
                                jensonId,
                                202008,
                                new NewOvertimeItemDto(LocalDate.now(), testData.projects.get("M1 Billing"), 4, null),
                                ctx))
                .expectNextCount(1).verifyComplete();
    }

    @Test
    public void testDeleteMyOvertime() {
        var jensonId = testData.employees.get(TestEmployees.FMS_Empl_Jenson_Curtis);
        var ctx = auth(TestEmployees.FMS_Empl_Jenson_Curtis).block(MONO_DEFAULT_TIMEOUT);
        var uuidComment = UUID.randomUUID().toString();
        StepVerifier
                .create(
                        overtimeService.addItem(
                                jensonId,
                                202008,
                                new NewOvertimeItemDto(LocalDate.now(), testData.projects.get("M1 Billing"), 7, uuidComment),
                                ctx)
                                .flatMap(r -> {
                                    var itemId = r.getItems().stream().filter(i -> uuidComment.equals(i.getNotes())).findFirst().get().getId();
                                    return overtimeService.deleteItem(jensonId, 202008, itemId, ctx);
                                })
                ).expectNextCount(1).verifyComplete();
    }


    @Test
    public void testAddOvertimeItemForEmployeeFromAnotherProject() {
        var jensonId = testData.employees.get(TestEmployees.FMS_Empl_Jenson_Curtis);
        var ctx = auth(TestEmployees.Billing_Manager_Maxwell_May).block(MONO_DEFAULT_TIMEOUT);
        StepVerifier
                .create(overtimeService.addItem(
                        jensonId,
                        202008,
                        new NewOvertimeItemDto(LocalDate.now(), testData.projects.get("M1 Billing"), 2, "testAddOvertimeItemForEmployeeFromAnotherProject"),
                        ctx))
                .expectError(AccessDeniedException.class).verify(MONO_DEFAULT_TIMEOUT);
    }

    @Test
    public void testAddOvertimeOfMyProject() {
        var jensonId = testData.employees.get(TestEmployees.FMS_Empl_Jenson_Curtis);
        var ctx = auth(TestEmployees.FMS_Manager_Jawad_Mcghee).block(MONO_DEFAULT_TIMEOUT);
        StepVerifier
                .create(overtimeService.addItem(
                        jensonId,
                        202008,
                        new NewOvertimeItemDto(LocalDate.now(), testData.projects.get("M1 Billing"), 6, "testAddOvertimeOfMyProject"),
                        ctx))
                .expectNextCount(1).verifyComplete();
    }


    private Mono<AuthContext> auth(String username) {
        return authHandler.login(new UsernamePasswordAuthenticationToken(username, securityProps.getMasterPassword()));
    }
}
