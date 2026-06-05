package ru.abondin.hreasy.platform.api.admin;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.service.overtime.OvertimeAdminService;

@RestController()
@RequestMapping("/api/v1/admin/overtimes")
@RequiredArgsConstructor
@Slf4j
public class OvertimeAdminController {

    private final OvertimeAdminService overtimeAdminService;

    @PostMapping("/{period}/close")
    public Mono<Integer> closeOvertimePeriod(@PathVariable int period,
                                             @RequestBody CloseOvertimePeriodBody body) {
        return AuthHandler.currentAuth().flatMap(auth ->
                overtimeAdminService.closeOvertimePeriod(auth, period, body.getComment()));
    }

    @PostMapping("/{period}/reopen")
    public Mono<Void> reopenOvertimePeriod(@PathVariable int period,
                                           @RequestBody ReopenOvertimePeriodBody body) {
        return AuthHandler.currentAuth().flatMap(auth ->
                overtimeAdminService.reopenOvertimePeriod(auth, period, body.getComment()));
    }


    @Data
    public static class CloseOvertimePeriodBody {
        @Schema(description = "Comment to close overtime period")
        @Nullable
        private String comment;
    }

    @Data
    public static class ReopenOvertimePeriodBody {
        @Schema(description = "Reopen closed overtime period reason")
        @Nullable
        private String comment;
    }

}
