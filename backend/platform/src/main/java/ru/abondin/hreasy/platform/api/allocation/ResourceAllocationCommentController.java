package ru.abondin.hreasy.platform.api.allocation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.service.allocation.ResourceAllocationCommentService;
import ru.abondin.hreasy.platform.service.allocation.dto.ResourceAllocationCommentDto;

import java.util.List;

/** Internal HTTP API for resource-allocation cell comments. */
@RestController
@RequestMapping("/api/v1/resource-allocations/comments")
@RequiredArgsConstructor
public class ResourceAllocationCommentController {
    private final ResourceAllocationCommentService service;

    @GetMapping("/summary/{year}")
    public Mono<ResourceAllocationCommentDto.SummaryDto> getSummary(@PathVariable int year) {
        return AuthHandler.currentAuth().flatMap(auth -> service.getSummary(year, auth));
    }

    @GetMapping
    public Mono<List<ResourceAllocationCommentDto>> getComments(@RequestParam int period,
                                                                 @RequestParam int employeeId,
                                                                 @RequestParam int projectId,
                                                                 @RequestParam(required = false) Integer workstreamId) {
        return AuthHandler.currentAuth().flatMap(auth ->
                service.getComments(period, employeeId, projectId, workstreamId, auth));
    }

    @PostMapping
    public Mono<ResourceAllocationCommentDto> create(@Valid @RequestBody ResourceAllocationCommentDto.CreateBody body) {
        return AuthHandler.currentAuth().flatMap(auth -> service.create(body, auth));
    }

    @PutMapping("/{commentId}")
    public Mono<ResourceAllocationCommentDto> update(@PathVariable int commentId,
                                                      @Valid @RequestBody ResourceAllocationCommentDto.UpdateBody body) {
        return AuthHandler.currentAuth().flatMap(auth -> service.update(commentId, body, auth));
    }

    @DeleteMapping("/{commentId}")
    public Mono<Void> delete(@PathVariable int commentId) {
        return AuthHandler.currentAuth().flatMap(auth -> service.delete(commentId, auth));
    }
}
