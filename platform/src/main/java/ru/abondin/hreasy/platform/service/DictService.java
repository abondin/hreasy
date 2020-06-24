package ru.abondin.hreasy.platform.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.dict.DictProjectRepo;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;
import ru.abondin.hreasy.platform.service.mapper.DictDtoMapper;

import java.time.OffsetDateTime;

@RequiredArgsConstructor
@Service
@Slf4j
public class DictService {

    private final DictProjectRepo projectRepo;
    private final DateTimeService dateTimeService;

    private final DictDtoMapper mapper;

    public Flux<SimpleDictDto> findProjects(AuthContext auth, boolean includeEnded) {
        OffsetDateTime endTime = null;
        if (includeEnded) {
            endTime = dateTimeService.now();
        }
        return projectRepo
                .findNotEnded(endTime)
                .map(e -> mapper.projectToDto(e));
    }
}
