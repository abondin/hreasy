package ru.abondin.hreasy.platform.service.admin.dict;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.dict.DictOfficeLocationLogRepo;
import ru.abondin.hreasy.platform.repo.dict.DictOfficeLocationRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.admin.dict.dto.AdminDictDtoMapper;
import ru.abondin.hreasy.platform.service.admin.dict.dto.CreateOrUpdateOfficeLocationBody;
import ru.abondin.hreasy.platform.service.dict.dto.DictLevelDto;
import ru.abondin.hreasy.platform.service.dict.dto.DictOfficeLocationDto;

@RequiredArgsConstructor
@Service
@Slf4j
public class AdminDictOfficeLocationService {

    private final DateTimeService dateTimeService;
    private final DictOfficeLocationRepo repo;
    private final DictOfficeLocationLogRepo logRepo;

    private final AdminDictDtoMapper mapper;
    private final AdminDictValidator secValidator;

    public Flux<DictOfficeLocationDto> findAll(AuthContext auth) {
        return secValidator.validateAdminOfficeLocation(auth)
                .flatMapMany(v -> repo.findAll())
                .map(mapper::fromEntry);
    }

    public Mono<DictOfficeLocationDto> create(AuthContext auth, CreateOrUpdateOfficeLocationBody body) {
        return doUpdate(auth, null, body);
    }

    public Mono<DictOfficeLocationDto> update(AuthContext auth, int id, CreateOrUpdateOfficeLocationBody body) {
        return doUpdate(auth, id, body);
    }

    public Mono<Integer> delete(AuthContext auth, int id) {
        log.info("Office Location level. Employee = {}. Id={}", auth.getUsername(), id);
        var now = dateTimeService.now();
        return secValidator.validateAdminOfficeLocation(auth)
                .flatMap(s -> repo.findById(id))
                .switchIfEmpty(Mono.error(new BusinessError("errors.entity.not.found", Integer.toString(id))))
                .flatMap(entry -> {
                    var history = mapper.toHistory(entry);
                    history.setCreatedAt(now);
                    history.setCreatedBy(auth.getEmployeeInfo().getEmployeeId());
                    history.setDeleted(true);
                    return logRepo.save(history).map(h -> id);
                });
    }


    private Mono<DictOfficeLocationDto> doUpdate(AuthContext auth, Integer id, CreateOrUpdateOfficeLocationBody body) {
        log.info("Update office location. Employee = {}. Id={}. RequestBody = {}", auth.getUsername(), id, body);
        var now = dateTimeService.now();
        var entry = mapper.toEntry(body);
        if (id != null) {
            entry.setId(id);
        }
        entry.setUpdatedAt(now);
        entry.setUpdatedBy(auth.getEmployeeInfo().getEmployeeId());
        return secValidator.validateAdminOfficeLocation(auth)
                .flatMap((v) -> repo
                        .save(entry)
                )
                .flatMap(persisted -> {
                    var history = mapper.toHistory(entry);
                    history.setCreatedAt(now);
                    history.setCreatedBy(auth.getEmployeeInfo().getEmployeeId());
                    history.setDeleted(false);
                    return logRepo.save(history).map((h) -> persisted);
                })
                .map(mapper::fromEntry);
    }


}
