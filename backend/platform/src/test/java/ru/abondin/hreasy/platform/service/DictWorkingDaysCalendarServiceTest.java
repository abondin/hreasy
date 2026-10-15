package ru.abondin.hreasy.platform.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import reactor.test.StepVerifier;
import ru.abondin.hreasy.platform.repo.PostgreSQLTestContainerContextInitializer;
import ru.abondin.hreasy.platform.repo.dict.DictWorkingDayCalendarRepo;
import ru.abondin.hreasy.platform.repo.dict.DictWorkingDaysCalendarEntry;
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

    @Autowired
    private DictWorkingDayCalendarRepo calendarRepo;

    @ParameterizedTest(name = "find({0}) returns exactly the calendars for {1}")
    @MethodSource("requestedCalendarYears")
    public void testFindReturnsExactlyRequestedExistingYears(List<Integer> requestedYears, List<Integer> expectedYears) {
        StepVerifier.create(calendarRepo.find(requestedYears, "RU", "default").collectList())
                .assertNext(calendars -> {
                    Assertions.assertEquals(expectedYears.size(), calendars.size(), "Number of calendar rows");
                    Assertions.assertEquals(expectedYears, calendars.stream()
                            .map(DictWorkingDaysCalendarEntry::getYear).sorted().toList(), "Exact returned years, including duplicates");
                    calendars.forEach(calendar -> {
                        Assertions.assertEquals("RU", calendar.getRegion());
                        Assertions.assertEquals("default", calendar.getType());
                    });
                })
                .expectComplete()
                .verify(MONO_DEFAULT_TIMEOUT);
    }

    private static Stream<Arguments> requestedCalendarYears() {
        return Stream.of(
                Arguments.of(List.of(2027), List.of(2027)),
                Arguments.of(List.of(2027, 2024), List.of(2024, 2027)),
                Arguments.of(List.of(2027, 2099, 2024), List.of(2024, 2027))
        );
    }

    @ParameterizedTest(name = "No calendar rows for region={0}, type={1}")
    @CsvSource({"UNKNOWN,default", "RU,unknown"})
    public void testFindReturnsNoRowsForUnknownRegionOrType(String region, String type) {
        StepVerifier.create(calendarRepo.find(List.of(2027), region, type).collectList())
                .assertNext(calendars -> Assertions.assertEquals(0, calendars.size(), "Number of calendar rows"))
                .expectComplete().verify(MONO_DEFAULT_TIMEOUT);
    }

    @Test
    public void testMultiYearVacationHolidaysExcludeTransfers() {
        var expected = Stream.of(2026, 2027)
                .flatMap(year -> List.of("01-01", "01-02", "01-03", "01-04", "01-05", "01-06", "01-07", "01-08",
                                "02-23", "03-08", "05-01", "05-09", "06-12", "11-04").stream()
                        .map(day -> LocalDate.parse(year + "-" + day)))
                .toList();
        StepVerifier.create(service.getDaysNotIncludedInVacations(List.of(2026, 2027)).sort().collectList())
                .assertNext(days -> {
                    Assertions.assertEquals(28, days.size(), "Fourteen holidays per requested year");
                    Assertions.assertEquals(expected, days, "Exact holidays from both years; no weekends or transfers");
                })
                .expectComplete()
                .verify(MONO_DEFAULT_TIMEOUT);
    }

    @Test
    public void test2027ReturnsExactlyAll118NonWorkingDates() {
        // Five-day working week, verified against https://www.consultant.ru/law/ref/calendar/proizvodstvennye/2027/
        var expected = Arrays.stream("""
                01-01 01-02 01-03 01-04 01-05 01-06 01-07 01-08 01-09 01-10 01-16 01-17 01-23 01-24 01-30 01-31
                02-06 02-07 02-13 02-14 02-21 02-22 02-23 02-27 02-28
                03-06 03-07 03-08 03-13 03-14 03-20 03-21 03-27 03-28
                04-03 04-04 04-10 04-11 04-17 04-18 04-24 04-25
                05-01 05-02 05-03 05-08 05-09 05-10 05-15 05-16 05-22 05-23 05-29 05-30
                06-05 06-06 06-12 06-13 06-14 06-19 06-20 06-26 06-27
                07-03 07-04 07-10 07-11 07-17 07-18 07-24 07-25 07-31
                08-01 08-07 08-08 08-14 08-15 08-21 08-22 08-28 08-29
                09-04 09-05 09-11 09-12 09-18 09-19 09-25 09-26
                10-02 10-03 10-09 10-10 10-16 10-17 10-23 10-24 10-30 10-31
                11-04 11-05 11-06 11-07 11-13 11-14 11-20 11-21 11-27 11-28
                12-04 12-05 12-11 12-12 12-18 12-19 12-25 12-26 12-31
                """.strip().split("\\s+"))
                .map(day -> LocalDate.parse("2027-" + day)).toList();
        StepVerifier.create(service.getNotWorkingDays(2027).collectList())
                .assertNext(days -> {
                    Assertions.assertEquals(118, days.size(), "Number of non-working dates");
                    Assertions.assertEquals(expected, days, "Exact non-working dates, including weekends and transfers");
                })
                .expectComplete()
                .verify(MONO_DEFAULT_TIMEOUT);
    }

    @ParameterizedTest
    @MethodSource("expectedDaysNotInVacation")
    public void testGetDaysNotIncludedInVacations(Map.Entry<Integer, List<String>> args) {
        var year = args.getKey();
        var days = args.getValue();
        var daysNotInVacation = days.stream().map(dayString -> LocalDate.parse(year + "-" + dayString)).toList();
        StepVerifier
                .create(service.getDaysNotIncludedInVacations(Arrays.asList(year)).collectList())
                .assertNext(actualDays -> {
                    Assertions.assertEquals(14, actualDays.size(), "Number of public holidays for " + year);
                    Assertions.assertEquals(daysNotInVacation, actualDays, "Exact public holidays for " + year);
                })
                .expectComplete()
                .verify(MONO_DEFAULT_TIMEOUT);
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

    /**
     * From https://its.1c.ru/calendar/work2023
     * @return
     */
    private static Stream<Map.Entry<Integer, List<String>>> expectedDaysNotInVacation() {
        return Stream.of(2022, 2023, 2024, 2025, 2026, 2027)
                .map(year -> Map.entry(year, List.of("01-01", "01-02", "01-03", "01-04", "01-05", "01-06", "01-07", "01-08",
                        "02-23", "03-08", "05-01", "05-09", "06-12", "11-04")));
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
