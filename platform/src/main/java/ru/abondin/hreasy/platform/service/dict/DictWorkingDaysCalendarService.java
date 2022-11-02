package ru.abondin.hreasy.platform.service.dict;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.config.HrEasyCommonProps;
import ru.abondin.hreasy.platform.repo.dict.DictWorkingDayCalendarRepo;
import ru.abondin.hreasy.platform.repo.dict.DictWorkingDaysCalendarEntry;
import ru.abondin.hreasy.platform.service.dict.dto.DictWorkingDaysCalendarDto;
import ru.abondin.hreasy.platform.service.dict.dto.DictWorkingDaysCalendarMapper;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ru.abondin.hreasy.platform.service.dict.dto.DictWorkingDaysCalendarDto.WorkingDayType.HOLIDAY;
import static ru.abondin.hreasy.platform.service.dict.dto.DictWorkingDaysCalendarDto.WorkingDayType.WORKING_DAY;

@RequiredArgsConstructor
@Service
@Slf4j
public class DictWorkingDaysCalendarService {
    private final DictWorkingDayCalendarRepo workingDayCalendarRepo;
    private final HrEasyCommonProps props;
    private final DictWorkingDaysCalendarMapper mapper;


    private Mono<List<DictWorkingDaysCalendarDto>> getCalendar(List<Integer> years) {
        String region = props.getDefaultCalendarRegion();
        String type = props.getDefaultCalendarType();
        return workingDayCalendarRepo.find(years, region, type)
                .map(DictWorkingDaysCalendarEntry::getCalendar)
                .map(mapper::calendarFromJson)
                .reduceWith(() -> new ArrayList<DictWorkingDaysCalendarDto>(), (c1, c2) -> {
                    c1.addAll(c2);
                    return c1;
                });
    }

    /**
     * @param years
     * @return days that should not be considered due vacation duration calculation
     */
    public Flux<LocalDate> getDaysNotIncludedInVacations(List<Integer> years) {
        return getCalendar(years).flatMapMany(days -> Flux.fromStream(days.stream()
                .filter(d ->
                        HOLIDAY.getType() == d.getType()
                )
                .map(DictWorkingDaysCalendarDto::getDay)
        ));
    }

    /**
     * @param year
     * @return <b>all</b> not working days of the year include all weekends
     */
    public Flux<LocalDate> getNotWorkingDays(int year) {
        return getCalendar(Arrays.asList(year)).flatMapMany(calendar -> {
            var allDays = LocalDate.ofYearDay(year, 1).datesUntil(LocalDate.ofYearDay(year + 1, 1));
            return Flux.fromStream(allDays).filter(d -> {
                var fromCalendar = calendar.stream().filter(c -> c.getDay().equals(d)).findFirst();
                if (weekends().contains(d.getDayOfWeek())) {
                    // Return any weekend except working days
                    return !fromCalendar.isPresent() || fromCalendar.get().getType() != WORKING_DAY.getType();
                } else {
                    // Check if it is a holiday or a moved holiday
                    return fromCalendar.isPresent() && DictWorkingDaysCalendarDto.WorkingDayType.isNotWorking(fromCalendar.get().getType());
                }
            });
        });
    }


    private List<DayOfWeek> weekends() {
        return Arrays.asList(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
    }
}
