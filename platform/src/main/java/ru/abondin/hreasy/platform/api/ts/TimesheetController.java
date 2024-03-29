package ru.abondin.hreasy.platform.api.ts;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.service.ts.TimesheetService;
import ru.abondin.hreasy.platform.service.ts.dto.TimesheetQueryFilter;
import ru.abondin.hreasy.platform.service.ts.dto.TimesheetReportBody;
import ru.abondin.hreasy.platform.service.ts.dto.TimesheetSummaryDto;

import java.time.LocalDate;

@RestController()
@RequestMapping("/api/v1/timesheet")
@RequiredArgsConstructor
@Slf4j
public class TimesheetController {

    private final TimesheetService service;

    @PostMapping("/{employeeId}")
    public Flux<Integer> report(@PathVariable int employeeId, @RequestBody TimesheetReportBody body) {
        return AuthHandler.currentAuth().flatMapMany(
                auth -> service.report(auth, employeeId, body));
    }

    @DeleteMapping("/{employeeId}/{recordId}")
    public Mono<Void> delete(Integer employeeId, Integer recordId) {
        return AuthHandler.currentAuth().flatMap(
                auth -> service.delete(auth, employeeId, recordId));
    }

    @GetMapping("")
    @ResponseBody
    public Flux<TimesheetSummaryDto> timesheetSummary(
            @RequestParam(name = "from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(name = "to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(name = "ba", required = false) Integer ba,
            @RequestParam(name = "project", required = false) Integer project
    ) {
        return AuthHandler.currentAuth().flatMapMany(
                auth -> service.timesheetSummary(auth, TimesheetQueryFilter.builder()
                        .from(from)
                        .to(to)
                        .ba(ba)
                        .project(project)
                        .build()));
    }

}