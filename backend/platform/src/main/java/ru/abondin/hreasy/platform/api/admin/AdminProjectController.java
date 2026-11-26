package ru.abondin.hreasy.platform.api.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.service.admin.ProjectAdminService;
import ru.abondin.hreasy.platform.service.admin.ProjectWorkstreamAdminService;
import ru.abondin.hreasy.platform.service.admin.dto.ProjectWorkstreamBody;
import ru.abondin.hreasy.platform.service.dto.ProjectWorkstreamDto;
import ru.abondin.hreasy.platform.service.admin.dto.ProjectDto;

@RestController()
@RequestMapping("/api/v1/admin/projects")
@RequiredArgsConstructor
@Slf4j
public class AdminProjectController {
    private final ProjectAdminService projectAdminService;
    private final ProjectWorkstreamAdminService workstreamService;

    @GetMapping("/{projectId}/workstreams")
    public Flux<ProjectWorkstreamDto> workstreams(@PathVariable int projectId) {
        return AuthHandler.currentAuth().flatMapMany(auth -> workstreamService.findAll(auth, projectId));
    }

    @PostMapping("/{projectId}/workstreams")
    public Mono<Integer> createWorkstream(@PathVariable int projectId, @RequestBody ProjectWorkstreamBody body) {
        return AuthHandler.currentAuth().flatMap(auth -> workstreamService.create(auth, projectId, body));
    }

    @PutMapping("/{projectId}/workstreams/{workstreamId}")
    public Mono<Integer> updateWorkstream(@PathVariable int projectId, @PathVariable int workstreamId,
                                         @RequestBody ProjectWorkstreamBody body) {
        return AuthHandler.currentAuth().flatMap(auth -> workstreamService.update(auth, projectId, workstreamId, body));
    }

    @DeleteMapping("/{projectId}/workstreams/{workstreamId}")
    public Mono<Integer> deleteWorkstream(@PathVariable int projectId, @PathVariable int workstreamId) {
        return AuthHandler.currentAuth().flatMap(auth -> workstreamService.delete(auth, projectId, workstreamId));
    }

    @PostMapping()
    public Mono<Integer> createProject(@RequestBody @Valid ProjectDto.CreateOrUpdateProjectDto body) {
        return AuthHandler.currentAuth().flatMap(auth ->
                projectAdminService.create(auth, body));
    }

    @PutMapping("/{projectId}")
    public Mono<Integer> updateProject(@PathVariable int projectId, @RequestBody @Valid ProjectDto.CreateOrUpdateProjectDto body) {
        return AuthHandler.currentAuth().flatMap(auth ->
                projectAdminService.update(auth, projectId, body));
    }

    @GetMapping()
    public Flux<ProjectDto> findAll() {
        return AuthHandler.currentAuth().flatMapMany(auth ->
                projectAdminService.findAll(auth));
    }

    @GetMapping("/{projectId}")
    public Mono<ProjectDto> findById(@PathVariable int projectId) {
        return AuthHandler.currentAuth().flatMap(auth ->
                projectAdminService.findById(auth, projectId));
    }

}
