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
import java.util.Arrays;
import java.util.List;

import static ru.abondin.hreasy.platform.service.dict.dto.DictWorkingDaysCalendarDto.WorkingDayType.HOLIDAY;

@RequiredArgsConstructor
@Service
@Slf4j
public class DictWorkingDaysCalendarService {
    private final DictWorkingDayCalendarRepo workingDayCalendarRepo;
    private final HrEasyCommonProps props;
    private final DictWorkingDaysCalendarMapper mapper;


    private Mono<List<DictWorkingDaysCalendarDto>> getCalendar(int year) {
        String region = props.getDefaultCalendarRegion();
        String type = props.getDefaultCalendarType();
        return workingDayCalendarRepo.findById(DictWorkingDaysCalendarEntry.WorkingDaysCalendarEntryPk.builder()
                        .region(region)
                        .type(type)
                        .year(year)
                        .build())
                .map(DictWorkingDaysCalendarEntry::getCalendar)
                .map(mapper::calendarFromJson);
    }

    /**
     * @param year
     * @return days that should not be considered due vacation duration calculation
     */
    public Flux<LocalDate> getDaysNotIncludedInVacations(int year) {
        return getCalendar(year).flatMapMany(days -> Flux.fromStream(days.stream()
                .filter(d ->
                        HOLIDAY.getType() == d.getType()
                                && !weekends().contains(d.getDay().getDayOfWeek())
                )
                .map(DictWorkingDaysCalendarDto::getDay)
        ));
    }


    private List<DayOfWeek> weekends() {
        return Arrays.asList(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
    }
}
