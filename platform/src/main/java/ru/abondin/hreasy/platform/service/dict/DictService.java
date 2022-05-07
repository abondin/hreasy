package ru.abondin.hreasy.platform.service.dict;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.dict.*;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;
import ru.abondin.hreasy.platform.service.mapper.DictDtoMapper;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Service
@Slf4j
public class DictService {

    private final DictProjectRepo projectRepo;
    private final DateTimeService dateTimeService;
    private final DepartmentRepo departmentRepo;
    private final DictPositionRepo positionRepo;
    private final DictLevelRepo levelRepo;
    private final DictOfficeLocationRepo officeLocationRepo;

    private final DictDtoMapper mapper;

    private final Sort defaultSimpleDictSort = Sort.by("name").ascending();

    public Flux<SimpleDictDto> findProjects(AuthContext auth) {
        var now = dateTimeService.now().toLocalDate();
        return projectRepo
                .findAll()
                .map(e -> {
                    var dto = mapper.projectToDto(e);
                    dto.setActive(e.getEndDate() == null || e.getEndDate().isAfter(now));
                    return dto;
                });
    }

    public Flux<SimpleDictDto> findDepartments(AuthContext auth) {
        return departmentRepo
                .findAll(defaultSimpleDictSort)
                .map(e -> {
                    var dto = new SimpleDictDto();
                    dto.setId(e.getId());
                    dto.setName(e.getName());
                    dto.setActive(true);
                    return dto;
                });
    }

    public Flux<SimpleDictDto> findPositions(AuthContext auth) {
        return positionRepo
                .findAll(defaultSimpleDictSort)
                .map(e -> {
                    var dto = new SimpleDictDto();
                    dto.setId(e.getId());
                    dto.setName(e.getName());
                    dto.setActive(true);
                    return dto;
                });
    }

    public Flux<SimpleDictDto> findLevels(AuthContext auth) {
        return levelRepo
                .findAll(defaultSimpleDictSort)
                .map(e -> {
                    var dto = new SimpleDictDto();
                    dto.setId(e.getId());
                    dto.setName(e.getName());
                    dto.setActive(true);
                    return dto;
                });
    }

    public Flux<SimpleDictDto> findOfficeLocations(AuthContext auth) {
        return officeLocationRepo
                .findAll(defaultSimpleDictSort)
                .map(e -> {
                    var dto = new SimpleDictDto();
                    dto.setId(e.getId());
                    dto.setName(
                            Stream.of(e.getName(), e.getDescription())
                                    .filter(i -> i != null)
                                    .collect(Collectors.joining(", ")));
                    dto.setActive(true);
                    return dto;
                });
    }
}
