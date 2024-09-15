package ru.abondin.hreasy.platform.api.dict;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.service.dict.DictService;
import ru.abondin.hreasy.platform.service.dict.DictWorkingDaysCalendarService;
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
    @ResponseBody
    public Flux<ProjectDictDto> projects() {
        return AuthHandler.currentAuth().flatMapMany(
                auth -> dictService.findProjects(auth));
    }

    @Operation(summary = "All departments")
    @GetMapping("/departments")
    @ResponseBody
    public Flux<SimpleDictDto> departments() {
        return AuthHandler.currentAuth().flatMapMany(
                auth -> dictService.findDepartments(auth));
    }

    @Operation(summary = "All organizations")
    @GetMapping("/organizations")
    @ResponseBody
    public Flux<SimpleDictDto> organizations() {
        return AuthHandler.currentAuth().flatMapMany(
                auth -> dictService.findOrganizations(auth));
    }

    @Operation(summary = "All employee positions (developer, QA, ect)")
    @GetMapping("/positions")
    @ResponseBody
    public Flux<SimpleDictDto> positions() {
        return AuthHandler.currentAuth().flatMapMany(
                auth -> dictService.findPositions(auth));
    }

    @Operation(summary = "All employee level (junior, middle, senior, ect)")
    @GetMapping("/levels")
    @ResponseBody
    public Flux<SimpleDictDto> levels() {
        return AuthHandler.currentAuth().flatMapMany(
                auth -> dictService.findLevels(auth));
    }

    @Operation(summary = "All offices")
    @GetMapping("/offices")
    @ResponseBody
    public Flux<SimpleDictDto> offices() {
        return AuthHandler.currentAuth().flatMapMany(
                auth -> dictService.findOffices(auth));
    }

    @Operation(summary = "All office locations")
    @GetMapping("/office_locations")
    @ResponseBody
    public Flux<SimpleDictDto> locations() {
        return AuthHandler.currentAuth().flatMapMany(
                auth -> dictService.findOfficeLocations(auth));
    }

    @Operation(summary = "All office locations")
    @GetMapping("/office_locations/{officeLocationId}/map")
    public Mono<String> getOfficeLocationMap(@PathVariable int officeLocationId) {
        return AuthHandler.currentAuth().flatMap(
                auth -> dictService.getOfficeLocationMap(auth, officeLocationId));
    }

    @Operation(summary = "All not working days")
    @GetMapping("/calendar/not_working_days/{year}")
    @ResponseBody
    public Flux<LocalDate> notWorkingDays(@PathVariable int year) {
        return calendarService.getNotWorkingDays(year);
    }

    @Operation(summary = "All not working days")
    @GetMapping("/calendar/days_not_included_in_vacations/{years}")
    @ResponseBody
    public Flux<LocalDate> daysNotIncludedInVacations(@PathVariable List<Integer> years) {
        return calendarService.getDaysNotIncludedInVacations(years);
    }

}
