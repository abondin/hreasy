package ru.abondin.hreasy.platform.service.admin.dict;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.dict.DepartmentLogRepo;
import ru.abondin.hreasy.platform.repo.dict.DepartmentRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.admin.dict.dto.AdminDictDtoMapper;
import ru.abondin.hreasy.platform.service.admin.dict.dto.CreateOrUpdateDepartmentBody;
import ru.abondin.hreasy.platform.service.dict.dto.DepartmentDto;

@RequiredArgsConstructor
@Service
@Slf4j
public class AdminDepartmentService {

    private final DateTimeService dateTimeService;
    private final DepartmentRepo repo;
    private final DepartmentLogRepo logRepo;

    private final AdminDictDtoMapper mapper;
    private final AdminDictValidator secValidator;

    public Flux<DepartmentDto> findAll(AuthContext auth) {
        return secValidator.validateAdminDepartment(auth)
                .flatMapMany(v -> repo.findAll())
                .map(mapper::fromEntry);
    }

    @Transactional
    public Mono<DepartmentDto> create(AuthContext auth, CreateOrUpdateDepartmentBody body) {
        return doUpdate(auth, null, body);
    }

    @Transactional
    public Mono<DepartmentDto> update(AuthContext auth, int id, CreateOrUpdateDepartmentBody body) {
        return doUpdate(auth, id, body);
    }

    private Mono<DepartmentDto> doUpdate(AuthContext auth, Integer id, CreateOrUpdateDepartmentBody body) {
        log.info("Update department. Employee = {}. Id={}. RequestBody = {}", auth.getUsername(), id, body);
        var now = dateTimeService.now();
        var entry = mapper.toEntry(body);
        if (id != null) {
            entry.setId(id);
        }
        entry.setUpdatedAt(now);
        entry.setUpdatedBy(auth.getEmployeeInfo().getEmployeeId());
        return secValidator.validateAdminDepartment(auth)
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

    /**
     * @param auth
     * @param id
     * @return
     * @deprecated Remove delete from DB functionality from API.
     * {@link ru.abondin.hreasy.platform.repo.dict.DepartmentEntry#setArchived(boolean)} and update method should be used instead
     */
    @Deprecated
    @Transactional
    public Mono<Integer> delete(AuthContext auth, int id) {
        log.info("Delete department. Employee = {}. Id={}", auth.getUsername(), id);
        var now = dateTimeService.now();
        return secValidator.validateAdminDepartment(auth)
                .flatMap(s -> repo.findById(id))
                .switchIfEmpty(Mono.error(new BusinessError("errors.entity.not.found", Integer.toString(id))))
                .flatMap(entry -> {
                    var history = mapper.toHistory(entry);
                    history.setCreatedAt(now);
                    history.setCreatedBy(auth.getEmployeeInfo().getEmployeeId());
                    return logRepo.save(history).map(h -> id);
                });
    }


}
