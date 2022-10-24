package ru.abondin.hreasy.platform.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import reactor.test.StepVerifier;
import ru.abondin.hreasy.platform.repo.PostgreSQLTestContainerContextInitializer;
import ru.abondin.hreasy.platform.service.dict.DictWorkingDaysCalendarService;

import java.time.Duration;
import java.time.LocalDate;
import java.util.stream.Stream;

@ActiveProfiles({"test"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ContextConfiguration(initializers = {PostgreSQLTestContainerContextInitializer.class})
@Slf4j
public class DictWorkingDaysCalendarServiceTest extends BaseServiceTest {

    private final static Duration MONO_DEFAULT_TIMEOUT = Duration.ofSeconds(3);


    @Autowired
    private DictWorkingDaysCalendarService service;


    @Test
    public void testGetDaysNotIncludedInVacations2022() {
        // In vacation includes all days (work days and weekends) except holiday
        var daysNotInVacation = Stream.of(
                "01-03", "01-04", "01-05", "01-06", "01-07",
                "02-23",
                "03-08",
                "05-09",
                "11-04"
        ).map(dayString -> LocalDate.parse("2022-" + dayString)).toList();
        StepVerifier
                .create(service.getDaysNotIncludedInVacations(2022).collectList())
                .expectNext(daysNotInVacation)
                .verifyComplete();
    }

    @Test
    public void testGetDaysNotIncludedInVacations2023() {
        // In vacation includes all days (work days and weekends) except holiday

    }

}
