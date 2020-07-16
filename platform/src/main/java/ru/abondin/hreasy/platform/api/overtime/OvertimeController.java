package ru.abondin.hreasy.platform.api.overtime;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.service.overtime.OvertimeService;
import ru.abondin.hreasy.platform.service.overtime.dto.NewOvertimeItemDto;
import ru.abondin.hreasy.platform.service.overtime.dto.OvertimeReportDto;

@RestController()
@RequestMapping("/api/v1/overtimes")
@RequiredArgsConstructor
@Slf4j
public class OvertimeController {

    private final OvertimeService service;

    @GetMapping("/{employeeId}/report/{period}")
    @ResponseBody
    public Mono<OvertimeReportDto> getOrStub(@PathVariable int employeeId,
                                             @PathVariable int period) {
        return service.getOrStub(employeeId, period);
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

}
