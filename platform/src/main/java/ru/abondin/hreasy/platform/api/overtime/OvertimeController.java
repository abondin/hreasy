package ru.abondin.hreasy.platform.api.overtime;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.service.overtime.OvertimeExportService;
import ru.abondin.hreasy.platform.service.overtime.OvertimeService;
import ru.abondin.hreasy.platform.service.overtime.dto.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@RestController()
@RequestMapping("/api/v1/overtimes")
@RequiredArgsConstructor
@Slf4j
public class OvertimeController {

    private final OvertimeService service;
    private final OvertimeExportService exportService;

    @GetMapping("/{employeeId}/report/{period}")
    @ResponseBody
    public Mono<OvertimeReportDto> getOrStub(@PathVariable int employeeId,
                                             @PathVariable int period) {
        return AuthHandler.currentAuth().flatMap(auth -> service.getOrStub(employeeId, period, auth));
    }

    @PostMapping("/{employeeId}/report/{period}/item")
    @ResponseBody
    public Mono<OvertimeReportDto> addNewItem(@PathVariable int employeeId,
                                              @PathVariable int period,
                                              @RequestBody NewOvertimeItemDto newItem) {
        log.debug("Adding {} to report [{}, {}]", newItem, employeeId, period);
        return AuthHandler.currentAuth().flatMap(auth -> service.addItem(employeeId, period, newItem, auth));
    }

    @DeleteMapping("/{employeeId}/report/{period}/item/{itemId}")
    @ResponseBody
    public Mono<OvertimeReportDto> deleteItem(@PathVariable int employeeId,
                                              @PathVariable int period,
                                              @PathVariable int itemId) {
        log.debug("Deleting item {} from report [{}, {}]", itemId, employeeId, period);
        return AuthHandler.currentAuth().flatMap(auth -> service.deleteItem(employeeId, period, itemId, auth));
    }

    @GetMapping("/closed-periods")
    @ResponseBody
    public Flux<OvertimeClosedPeriodDto> getClosedPeriods() {
        return service.getClosedPeriods();
    }

    @GetMapping("/summary/{period}")
    @ResponseBody
    public Flux<OvertimeEmployeeSummary> getSummary(@PathVariable int period) {
        return AuthHandler.currentAuth().flatMapMany(auth -> service.getSummary(period, auth));
    }


    @PostMapping("/{employeeId}/report/{period}/approve")
    @ResponseBody
    @Valid
    public Mono<OvertimeReportDto> approveReport(@PathVariable int employeeId,
                                                 @PathVariable int period,
                                                 @RequestBody ApproveReportBody body) {
        return AuthHandler.currentAuth().flatMap(auth -> service.approveReport(employeeId,
                period,
                OvertimeApprovalDecisionDto.ApprovalDecision.APPROVED,
                body.previousApprovalId,
                body.comment,
                auth));
    }


    @PostMapping("/{employeeId}/report/{period}/decline")
    @ResponseBody
    @Valid
    public Mono<OvertimeReportDto> declineReport(@PathVariable int employeeId,
                                                 @PathVariable int period,
                                                 @RequestBody DeclineReportBody body) {
        return AuthHandler.currentAuth().flatMap(auth -> service.approveReport(employeeId,
                period,
                OvertimeApprovalDecisionDto.ApprovalDecision.DECLINED,
                body.previousApprovalId,
                body.comment,
                auth));
    }

    @GetMapping("/summary/{period}/export")
    public Mono<Resource> export(@PathVariable int period) {
        return AuthHandler.currentAuth().flatMap(auth -> exportService.export(period, auth));
    }

    @Data
    @Valid
    public static class ApproveReportBody {
        @Nullable
        private String comment;
        @Nullable
        private Integer previousApprovalId;
    }

    @Data
    @Valid
    public static class DeclineReportBody {
        @NotNull(message = "Comment is required to decline overtime report")
        private String comment;
        @Nullable
        private Integer previousApprovalId;
    }
}
