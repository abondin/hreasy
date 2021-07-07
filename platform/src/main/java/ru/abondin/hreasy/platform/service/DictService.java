package ru.abondin.hreasy.platform.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.dict.DepartmentRepo;
import ru.abondin.hreasy.platform.repo.dict.DictLevelRepo;
import ru.abondin.hreasy.platform.repo.dict.DictPositionRepo;
import ru.abondin.hreasy.platform.repo.dict.DictProjectRepo;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;
import ru.abondin.hreasy.platform.service.mapper.DictDtoMapper;

import java.util.Comparator;

@RequiredArgsConstructor
@Service
@Slf4j
public class DictService {

    private final DictProjectRepo projectRepo;
    private final DateTimeService dateTimeService;
    private final DepartmentRepo departmentRepo;
    private final DictPositionRepo positionRepo;
    private final DictLevelRepo levelRepo;

    private final DictDtoMapper mapper;

    public Flux<SimpleDictDto> findProjects(AuthContext auth) {
        var now = dateTimeService.now().toLocalDate();
        return projectRepo
                .findAll()
                .map(e -> {
                    var dto = mapper.projectToDto(e);
                    dto.setActive(e.getEndDate() == null || e.getEndDate().isAfter(now));
                    return dto;
                })
                .sort(Comparator.comparing(SimpleDictDto::getName));
    }

    public Publisher<? extends SimpleDictDto> findDepartments(AuthContext auth) {
        return departmentRepo
                .findAll()
                .map(e -> {
                    var dto = new SimpleDictDto();
                    dto.setId(e.getId());
                    dto.setName(e.getName());
                    dto.setActive(true);
                    return dto;
                })
                .sort(Comparator.comparing(SimpleDictDto::getName));
    }

    public Publisher<? extends SimpleDictDto> findPositions(AuthContext auth) {
        return positionRepo
                .findAll()
                .map(e -> {
                    var dto = new SimpleDictDto();
                    dto.setId(e.getId());
                    dto.setName(e.getName());
                    dto.setActive(true);
                    return dto;
                })
                .sort(Comparator.comparing(SimpleDictDto::getName));
    }

    public Publisher<? extends SimpleDictDto> findLevels(AuthContext auth) {
        return levelRepo
                .findAll()
                .map(e -> {
                    var dto = new SimpleDictDto();
                    dto.setId(e.getId());
                    dto.setName(e.getName());
                    dto.setActive(true);
                    return dto;
                })
                .sort(Comparator.comparing(SimpleDictDto::getName));
    }
}
