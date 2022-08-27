package ru.abondin.hreasy.platform.api.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.service.admin.ProjectAdminService;
import ru.abondin.hreasy.platform.service.admin.dto.ProjectDto;

@RestController()
@RequestMapping("/api/v1/admin/projects")
@RequiredArgsConstructor
@Slf4j
public class AdminProjectController {
    private final ProjectAdminService projectAdminService;

    @PostMapping()
    public Mono<Integer> createProject(@RequestBody ProjectDto.CreateOrUpdateProjectDto body) {
        return AuthHandler.currentAuth().flatMap(auth ->
                projectAdminService.create(auth, body));
    }

    @PutMapping("/{projectId}")
    public Mono<Integer> updateProject(@PathVariable int projectId, @RequestBody ProjectDto.CreateOrUpdateProjectDto body) {
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
