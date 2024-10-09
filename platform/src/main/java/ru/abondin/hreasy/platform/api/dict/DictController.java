package ru.abondin.hreasy.platform.api.dict;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.service.dict.DictService;
import ru.abondin.hreasy.platform.service.dict.DictWorkingDaysCalendarService;
import ru.abondin.hreasy.platform.service.dict.dto.DictOfficeLocationMap;
import ru.abondin.hreasy.platform.service.dto.OfficeLocationDictDto;
import ru.abondin.hreasy.platform.service.dto.ProjectDictDto;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;

import java.time.LocalDate;
import java.util.List;

@RestController()
@RequestMapping("/api/v1/dict")
@RequiredArgsConstructor
@Slf4j
public class DictController {

    private final DictService dictService;
    private final DictWorkingDaysCalendarService calendarService;

    @Operation(summary = "All projects")
    @GetMapping("/projects")
    public Flux<ProjectDictDto> projects() {
        return AuthHandler.currentAuth().flatMapMany(
                dictService::findProjects);
    }

    @Operation(summary = "All departments")
    @GetMapping("/departments")
    public Flux<SimpleDictDto> departments() {
        return AuthHandler.currentAuth().flatMapMany(
                dictService::findDepartments);
    }

    @Operation(summary = "All organizations")
    @GetMapping("/organizations")
    public Flux<SimpleDictDto> organizations() {
        return AuthHandler.currentAuth().flatMapMany(
                dictService::findOrganizations);
    }

    @Operation(summary = "All employee positions (developer, QA, ect)")
    @GetMapping("/positions")
    public Flux<SimpleDictDto> positions() {
        return AuthHandler.currentAuth().flatMapMany(
                dictService::findPositions);
    }

    @Operation(summary = "All employee level (junior, middle, senior, ect)")
    @GetMapping("/levels")
    public Flux<SimpleDictDto> levels() {
        return AuthHandler.currentAuth().flatMapMany(
                dictService::findLevels);
    }

    @Operation(summary = "All offices")
    @GetMapping("/offices")
    public Flux<SimpleDictDto> offices() {
        return AuthHandler.currentAuth().flatMapMany(
                dictService::findOffices);
    }

    @Operation(summary = "All office locations")
    @GetMapping("/office_locations")
    public Flux<OfficeLocationDictDto> locations() {
        return AuthHandler.currentAuth().flatMapMany(
                dictService::findOfficeLocations);
    }


    @Operation(summary = "Open office or office location SVG map")
    @GetMapping("/office_maps")
    public Flux<DictOfficeLocationMap> getOfficeLocationMaps() {
        return AuthHandler.currentAuth().flatMapMany(
                auth -> dictService.getOfficeLocationMaps(auth));
    }

    @Operation(summary = "Open office or office location SVG map")
    @GetMapping("/office_maps/{filename}")
    public Mono<String> getOfficeLocationMapFile(@PathVariable String filename) {
        return AuthHandler.currentAuth().flatMap(
                auth -> dictService.getOfficeLocationMap(auth, filename));
    }

    @Operation(summary = "All not working days")
    @GetMapping("/calendar/not_working_days/{year}")
    public Flux<LocalDate> notWorkingDays(@PathVariable int year) {
        return calendarService.getNotWorkingDays(year);
    }

    @Operation(summary = "All not working days")
    @GetMapping("/calendar/days_not_included_in_vacations/{years}")
    public Flux<LocalDate> daysNotIncludedInVacations(@PathVariable List<Integer> years) {
        return calendarService.getDaysNotIncludedInVacations(years);
    }

}
