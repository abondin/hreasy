package ru.abondin.hreasy.platform.service.dict;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.dict.*;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.dto.OfficeLocationDictDto;
import ru.abondin.hreasy.platform.service.dto.ProjectDictDto;
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
    private final DictOrganizationRepo organizationRepo;
    private final DictPositionRepo positionRepo;
    private final DictLevelRepo levelRepo;
    private final DictOfficeLocationRepo officeLocationRepo;
    private final DictOfficeRepo officeRepo;

    private final DictDtoMapper mapper;


    public Flux<ProjectDictDto> findProjects(AuthContext auth) {
        log.trace("Get all projects {}", auth.getUsername());
        var now = dateTimeService.now().toLocalDate();
        return projectRepo
                .findAll()
                .map(e -> {
                    var dto = mapper.projectToDto(e);
                    dto.setActive(e.getEndDate() == null || e.getEndDate().isAfter(now));
                    return dto;
                });
    }

    public Flux<SimpleDictDto> findOrganizations(AuthContext auth) {
        log.trace("Get all organizations {}", auth.getUsername());
        return organizationRepo
                .findAll()
                .map(e -> {
                    var dto = new SimpleDictDto();
                    dto.setId(e.getId());
                    dto.setName(e.getName());
                    dto.setActive(!e.isArchived());
                    return dto;
                });
    }

    public Flux<SimpleDictDto> findDepartments(AuthContext auth) {
        log.trace("Get all departments {}", auth.getUsername());
        return departmentRepo
                .findNotArchived()
                .map(e -> {
                    var dto = new SimpleDictDto();
                    dto.setId(e.getId());
                    dto.setName(e.getName());
                    dto.setActive(!e.isArchived());
                    return dto;
                });
    }

    public Flux<SimpleDictDto> findPositions(AuthContext auth) {
        log.trace("Get all positions {}", auth.getUsername());
        return positionRepo
                .findNotArchived()
                .map(e -> {
                    var dto = new SimpleDictDto();
                    dto.setId(e.getId());
                    dto.setName(e.getName());
                    dto.setActive(!e.isArchived());
                    return dto;
                });
    }

    public Flux<SimpleDictDto> findLevels(AuthContext auth) {
        log.trace("Get all levels {}", auth.getUsername());
        return levelRepo
                .findNotArchived()
                .map(e -> {
                    var dto = new SimpleDictDto();
                    dto.setId(e.getId());
                    dto.setName(e.getName());
                    dto.setActive(!e.isArchived());
                    return dto;
                });
    }

    public Flux<SimpleDictDto> findOffices(AuthContext auth) {
        log.trace("Get all offices {}", auth.getUsername());
        return officeRepo
                .findNotArchived()
                .map(e -> {
                    var dto = new SimpleDictDto();
                    dto.setId(e.getId());
                    dto.setName(e.getName());
                    dto.setActive(!e.isArchived());
                    return dto;
                });
    }

    public Flux<OfficeLocationDictDto> findOfficeLocations(AuthContext auth) {
        log.trace("Get all office locations {}", auth.getUsername());
        return officeLocationRepo
                .findNotArchived()
                .map(e -> {
                    var dto = new OfficeLocationDictDto();
                    dto.setId(e.getId());
                    dto.setOfficeId(e.getOfficeId());
                    dto.setName(
                            Stream.of(e.getName(), e.getDescription())
                                    .filter(i -> i != null)
                                    .collect(Collectors.joining(", ")));
                    dto.setActive(!e.isArchived());
                    return dto;
                });
    }

    public Mono<String> getOfficeLocationMap(AuthContext auth, int officeLocationId) {
        log.trace("Get office location map {} by {}", officeLocationId, auth.getUsername());
        return officeLocationRepo
                .findNotArchived(officeLocationId).filter(l -> l.getMapSvg() != null).map(DictOfficeLocationEntry::getMapSvg);
    }
}
