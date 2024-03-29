package ru.abondin.hreasy.platform.service.admin.dict;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.dict.DictOrganizationLogRepo;
import ru.abondin.hreasy.platform.repo.dict.DictOrganizationRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.admin.dict.dto.AdminDictDtoMapper;
import ru.abondin.hreasy.platform.service.admin.dict.dto.CreateOrUpdateOrganizationBody;
import ru.abondin.hreasy.platform.service.dict.dto.DictOrganizationDto;

/**
 * @author Alexander Bondin
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class AdminDictOrganizationService {

    private final DateTimeService dateTimeService;
    private final DictOrganizationRepo repo;
    private final DictOrganizationLogRepo logRepo;

    private final AdminDictDtoMapper mapper;
    private final AdminDictValidator secValidator;


    public Flux<DictOrganizationDto> findAll(AuthContext auth) {
        return secValidator.validateAdminOrganizations(auth)
                .flatMapMany(v -> repo.findAll())
                .map(mapper::fromEntry);
    }


    @Transactional
    public Mono<DictOrganizationDto> create(AuthContext auth, CreateOrUpdateOrganizationBody body) {
        return doUpdate(auth, null, body);
    }

    @Transactional
    public Mono<DictOrganizationDto> update(AuthContext auth, int id, CreateOrUpdateOrganizationBody body) {
        return doUpdate(auth, id, body);
    }


    private Mono<DictOrganizationDto> doUpdate(AuthContext auth, Integer id, CreateOrUpdateOrganizationBody body) {
        log.info("Create/Update organization. Employee = {}. Id={}. RequestBody = {}", auth.getUsername(), id, body);
        var now = dateTimeService.now();
        var entry = mapper.toEntry(body);
        if (id != null) {
            entry.setId(id);
        }
        entry.setUpdatedAt(now);
        entry.setUpdatedBy(auth.getEmployeeInfo().getEmployeeId());
        return secValidator.validateAdminOrganizations(auth)
                .flatMap((v) -> repo
                        .save(entry)
                )
                .flatMap(persisted -> {
                    var history = mapper.toHistory(persisted);
                    history.setCreatedAt(now);
                    history.setCreatedBy(auth.getEmployeeInfo().getEmployeeId());
                    return logRepo.save(history).map((h) -> persisted);
                })
                .map(mapper::fromEntry);
    }


}
