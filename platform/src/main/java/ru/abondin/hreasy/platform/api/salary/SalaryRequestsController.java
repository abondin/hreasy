package ru.abondin.hreasy.platform.api.salary;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.service.salary.SalaryRequestService;
import ru.abondin.hreasy.platform.service.salary.dto.SalaryRequestClosedPeriodDto;
import ru.abondin.hreasy.platform.service.salary.dto.SalaryRequestDto;
import ru.abondin.hreasy.platform.service.salary.dto.SalaryRequestReportBody;
import ru.abondin.hreasy.platform.service.salary.dto.approval.SalaryRequestApproveBody;
import ru.abondin.hreasy.platform.service.salary.dto.approval.SalaryRequestCommentBody;
import ru.abondin.hreasy.platform.service.salary.dto.approval.SalaryRequestDeclineBody;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/salaries/requests")
public class SalaryRequestsController {
    private final SalaryRequestService requestService;

    @PostMapping()
    public Mono<Integer> report(@RequestBody SalaryRequestReportBody body) {
        return AuthHandler.currentAuth().flatMap(auth -> requestService.report(auth, body));
    }

    @DeleteMapping("/{requestId}")
    public Mono<Integer> delete(@PathVariable int requestId) {
        return AuthHandler.currentAuth().flatMap(auth -> requestService.delete(auth, requestId));
    }

    @PostMapping("/{salaryRequestId}/approvals/approve")
    public Mono<Integer> approve(@PathVariable int requestId, @RequestBody SalaryRequestApproveBody body) {
        return AuthHandler.currentAuth().flatMap(auth -> requestService.approve(auth, requestId, body));
    }
    @PostMapping("/{salaryRequestId}/approvals/decline")
    public Mono<Integer> decline(@PathVariable int requestId, @RequestBody SalaryRequestDeclineBody body) {
        return AuthHandler.currentAuth().flatMap(auth -> requestService.decline(auth, requestId, body));
    }
    @PostMapping("/{salaryRequestId}/approvals/comment")
    public Mono<Integer> comment(@PathVariable int requestId, @RequestBody SalaryRequestCommentBody body) {
        return AuthHandler.currentAuth().flatMap(auth -> requestService.comment(auth, requestId, body));
    }

    /**
     * Delete own approve, decline decision or just comment
     * @param requestId
     * @param approvalId
     * @return
     * @see #approve(int, SalaryRequestApproveBody) {@link #decline(int, SalaryRequestDeclineBody)} {@link #comment(int, SalaryRequestCommentBody)}
     */
    @DeleteMapping("/{salaryRequestId}//approvals/{approvalId}")
    public Mono<Integer> deleteApproval(@PathVariable int requestId, @PathVariable int approvalId) {
        return AuthHandler.currentAuth().flatMap(auth -> requestService.deleteApproval(auth, requestId, approvalId));
    }

    @GetMapping("/{period}")
    public Flux<SalaryRequestDto> my(@PathVariable int period) {
        return AuthHandler.currentAuth().flatMapMany(auth -> requestService.findMy(auth, period));
    }

    @GetMapping("/{period}/{requestId}")
    public Mono<SalaryRequestDto> get(@PathVariable int period, @PathVariable int requestId) {
        return AuthHandler.currentAuth().flatMap(auth -> requestService.get(auth, requestId));
    }


    @GetMapping("/periods")
    public Flux<SalaryRequestClosedPeriodDto> getClosedPeriods() {
        return AuthHandler.currentAuth().flatMapMany(auth ->
                requestService.getClosedSalaryRequestPeriods(auth));
    }
}
