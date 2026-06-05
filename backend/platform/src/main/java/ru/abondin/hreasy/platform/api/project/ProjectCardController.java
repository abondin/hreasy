package ru.abondin.hreasy.platform.api.project;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.service.project.ProjectCardService;
import ru.abondin.hreasy.platform.service.project.dto.ProjectCardInfoDto;

@RestController()
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Slf4j
public class ProjectCardController {

    private final ProjectCardService projectCardService;

    @Operation(summary = "Full project information including managers for project card")
    @GetMapping("/{projectId}")
    @ResponseBody
    public Mono<ProjectCardInfoDto> fullProjectInfo(@PathVariable int projectId) {
        return AuthHandler.currentAuth().flatMap(
                auth -> projectCardService.getProjectInfo(auth, projectId));
    }
}
