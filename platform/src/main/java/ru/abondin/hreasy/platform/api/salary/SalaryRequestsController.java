package ru.abondin.hreasy.platform.api.salary;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.service.salary.SalaryRequestService;
import ru.abondin.hreasy.platform.service.salary.dto.*;
import ru.abondin.hreasy.platform.service.salary.dto.approval.SalaryRequestApprovalDto;
import ru.abondin.hreasy.platform.service.salary.dto.approval.SalaryRequestApproveBody;
import ru.abondin.hreasy.platform.service.salary.dto.approval.SalaryRequestCommentBody;
import ru.abondin.hreasy.platform.service.salary.dto.approval.SalaryRequestDeclineBody;
import ru.abondin.hreasy.platform.service.salary.dto.link.SalaryRequestLinkCreateBody;

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

    @GetMapping("/{salaryRequestId}/approvals")
    public Flux<SalaryRequestApprovalDto> approvals(@PathVariable int salaryRequestId) {
        return AuthHandler.currentAuth().flatMapMany(auth -> requestService.findApprovals(auth, salaryRequestId));
    }

    @DeleteMapping("/{requestId}")
    public Mono<Integer> delete(@PathVariable int requestId) {
        return AuthHandler.currentAuth().flatMap(auth -> requestService.delete(auth, requestId));
    }

    @PutMapping("/{requestId}")
    public Mono<Integer> update(@PathVariable int requestId, @RequestBody SalaryRequestUpdateBody body) {
        return AuthHandler.currentAuth().flatMap(auth -> requestService.update(auth, requestId, body));
    }

    @PostMapping("/{salaryRequestId}/approvals/approve")
    public Mono<Integer> approve(@PathVariable int salaryRequestId, @RequestBody SalaryRequestApproveBody body) {
        return AuthHandler.currentAuth().flatMap(auth -> requestService.approve(auth, salaryRequestId, body));
    }

    @PostMapping("/{salaryRequestId}/approvals/decline")
    public Mono<Integer> decline(@PathVariable int salaryRequestId, @RequestBody SalaryRequestDeclineBody body) {
        return AuthHandler.currentAuth().flatMap(auth -> requestService.decline(auth, salaryRequestId, body));
    }

    @PostMapping("/{salaryRequestId}/approvals/comment")
    public Mono<Integer> comment(@PathVariable int salaryRequestId, @RequestBody SalaryRequestCommentBody body) {
        return AuthHandler.currentAuth().flatMap(auth -> requestService.comment(auth, salaryRequestId, body));
    }

    /**
     * Delete own approve, decline decision or just comment
     *
     * @param salaryRequestId
     * @param approvalId
     * @return
     * @see #approve(int, SalaryRequestApproveBody) {@link #decline(int, SalaryRequestDeclineBody)} {@link #comment(int, SalaryRequestCommentBody)}
     */
    @DeleteMapping("/{salaryRequestId}/approvals/{approvalId}")
    public Mono<Integer> deleteApproval(@PathVariable int salaryRequestId, @PathVariable int approvalId) {
        return AuthHandler.currentAuth().flatMap(auth -> requestService.deleteApproval(auth, salaryRequestId, approvalId));
    }

    @PostMapping("/links")
    public Mono<Integer> addLink(@RequestBody SalaryRequestLinkCreateBody body) {
        return AuthHandler.currentAuth().flatMap(auth -> requestService.addLink(auth, body));
    }


    @GetMapping("/{period}")
    public Flux<SalaryRequestDto> find(@PathVariable int period) {
        return AuthHandler.currentAuth().flatMapMany(auth -> requestService.find(auth, period));
    }

    /**
     * For each employee who has not been dismissed, this endpoint returns their latest salary request (excluding bonuses).
     * If no such request exists, the related request fields will be null.
     *
     * @return
     */
    @GetMapping("/latest")
    public Flux<EmployeeWithLatestSalaryRequestDto> findLatestIncreases() {
        return AuthHandler.currentAuth().flatMapMany(auth -> requestService.findLatestIncreases(auth));
    }

    @GetMapping("/{period}/{requestId}")
    public Mono<SalaryRequestDto> get(@PathVariable int period, @PathVariable int requestId) {
        return AuthHandler.currentAuth().flatMap(auth -> requestService.get(auth, requestId));
    }


    @GetMapping("/periods")
    public Flux<SalaryRequestClosedPeriodDto> getClosedPeriods() {
        return AuthHandler.currentAuth().flatMapMany(auth ->
                requestService.getClosedSalaryRequestPeriods());
    }
}
