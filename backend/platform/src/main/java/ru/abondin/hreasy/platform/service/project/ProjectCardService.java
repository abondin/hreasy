package ru.abondin.hreasy.platform.service.project;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.dict.DictProjectRepo;
import ru.abondin.hreasy.platform.repo.dict.ProjectWorkstreamRepo;
import ru.abondin.hreasy.platform.service.dto.ProjectWorkstreamDto;
import ru.abondin.hreasy.platform.service.project.dto.ProjectCardInfoDto;
import ru.abondin.hreasy.platform.service.project.dto.ProjectCardMapper;

/**
 * Get full information about project to show project card
 */
@Service
@RequiredArgsConstructor
@Slf4j

public class ProjectCardService {
    private final DictProjectRepo repo;
    private final ProjectCardMapper mapper;
    private final ProjectWorkstreamRepo workstreamRepo;

    public Mono<ProjectCardInfoDto> getProjectInfo(AuthContext auth, int projectId) {
        return repo.findFullInfoWithManagersById(projectId).flatMap(entry ->
                workstreamRepo.findActiveByProjectId(projectId)
                        .map(workstream -> new ProjectWorkstreamDto(workstream.getId(), workstream.getExternalId(),
                                workstream.getDisplayName(), workstream.getDescription()))
                        .collectList()
                        .map(workstreams -> {
                            var result = mapper.fromEntry(entry);
                            result.setWorkstreams(workstreams);
                            return result;
                        }));
    }


}
