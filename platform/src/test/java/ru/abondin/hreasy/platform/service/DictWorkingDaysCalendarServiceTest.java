package ru.abondin.hreasy.platform.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import reactor.test.StepVerifier;
import ru.abondin.hreasy.platform.repo.PostgreSQLTestContainerContextInitializer;
import ru.abondin.hreasy.platform.service.dict.DictWorkingDaysCalendarService;

import java.time.Duration;
import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@ActiveProfiles({"test"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ContextConfiguration(initializers = {PostgreSQLTestContainerContextInitializer.class})
@Slf4j
public class DictWorkingDaysCalendarServiceTest extends BaseServiceTest {

    private final static Duration MONO_DEFAULT_TIMEOUT = Duration.ofSeconds(3);


    @Autowired
    private DictWorkingDaysCalendarService service;


    @ParameterizedTest
    @MethodSource("expectedDaysNotInVacation")
    public void testGetDaysNotIncludedInVacations(Map.Entry<Integer, List<String>> args) {
        var year = args.getKey();
        var days = args.getValue();
        var daysNotInVacation = days.stream().map(dayString -> LocalDate.parse(year + "-" + dayString)).toList();
        StepVerifier
                .create(service.getDaysNotIncludedInVacations(Arrays.asList(year)).collectList())
                .expectNext(daysNotInVacation)
                .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("expectedNumberOfWorkingDays")
    public void testCalculateWorkingDays(int[] args) {
        var year = args[0];
        var month = args[1];
        var expectedDays = args[2];
        var expectedWorkingDays = args[3];
        StepVerifier
                .create(service.getNotWorkingDays(year)
                        .filter(d -> d.getYear() == year && d.getMonthValue() == month)
                        .collectList())
                .assertNext(notWorkingDays -> {
                    Assertions.assertEquals(expectedDays - expectedWorkingDays, notWorkingDays.size(),
                            year + " " + Month.of(month) + ": Total days " + expectedDays + " minus " + expectedWorkingDays + " working days: " + (expectedDays - expectedWorkingDays));
                })
                .verifyComplete();
    }

    private static Stream<Map.Entry<Integer, List<String>>> expectedDaysNotInVacation() {
        return Stream.of(
                Map.entry(2022, Arrays.asList("01-03", "01-04", "01-05", "01-06", "01-07",
                        "02-23",
                        "03-08",
                        "05-09",
                        "11-04")),
                Map.entry(2023, Arrays.asList("01-02", "01-03", "01-04", "01-05", "01-06",
                        "02-23",
                        "03-08",
                        "05-01", "05-09",
                        "06-12"))

        );
    }

    /**
     * Data to verify got from <a href="https://www.garant.ru/calendar/buhpravo/">garant.ru</a>
     *
     * @return [year, month, number of days, number of working days]
     */
    private static Stream<int[]> expectedNumberOfWorkingDays() {
        return Stream.of(
                new int[]{2022, 1, 31, 16}
                , new int[]{2022, 2, 28, 19}
                , new int[]{2022, 3, 31, 22}
                , new int[]{2022, 4, 30, 21}
                , new int[]{2022, 5, 31, 18}
                , new int[]{2022, 6, 30, 21}
                , new int[]{2022, 7, 31, 21}
                , new int[]{2022, 8, 31, 23}
                , new int[]{2022, 9, 30, 22}
                , new int[]{2022, 10, 31, 21}
                , new int[]{2022, 11, 30, 21}
                , new int[]{2022, 12, 31, 22}

                , new int[]{2023, 1, 31, 17}
                , new int[]{2023, 2, 28, 18}
                , new int[]{2023, 3, 31, 22}
                , new int[]{2023, 4, 30, 20}
                , new int[]{2023, 5, 31, 20}
                , new int[]{2023, 6, 30, 21}
                , new int[]{2023, 7, 31, 21}
                , new int[]{2023, 8, 31, 23}
                , new int[]{2023, 9, 30, 21}
                , new int[]{2023, 10, 31, 22}
                , new int[]{2023, 11, 30, 21}
                , new int[]{2023, 12, 31, 21}
        );
    }


}
