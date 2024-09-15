package ru.abondin.hreasy.platform.service.admin.dict;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.dict.DictOfficeLocationRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.HistoryDomainService;
import ru.abondin.hreasy.platform.service.admin.dict.dto.AdminDictDtoMapper;
import ru.abondin.hreasy.platform.service.admin.dict.dto.CreateOrUpdateOfficeLocationBody;
import ru.abondin.hreasy.platform.service.dict.dto.DictOfficeLocationDto;

import static ru.abondin.hreasy.platform.service.HistoryDomainService.HistoryEntityType.OFFICE_LOCATION;

@RequiredArgsConstructor
@Service
@Slf4j
public class AdminDictOfficeLocationService {

    private final DateTimeService dateTimeService;
    private final DictOfficeLocationRepo repo;
    private final HistoryDomainService history;

    private final AdminDictDtoMapper mapper;
    private final AdminDictValidator secValidator;

    public Flux<DictOfficeLocationDto> findAll(AuthContext auth) {
        return secValidator.validateAdminOfficeLocation(auth)
                .flatMapMany(v -> repo.findAllView())
                .map(mapper::fromEntry);
    }

    @Transactional
    public Mono<Integer> create(AuthContext auth, CreateOrUpdateOfficeLocationBody body) {
        return doUpdate(auth, null, body);
    }

    @Transactional
    public Mono<Integer> update(AuthContext auth, int id, CreateOrUpdateOfficeLocationBody body) {
        return doUpdate(auth, id, body);
    }

    /**
     * @param auth
     * @param id
     * @return
     * @deprecated Remove delete from DB functionality from API.
     * {@link ru.abondin.hreasy.platform.repo.dict.DictOfficeLocationEntry#setArchived(boolean)} and update method should be used instead
     */
    @Deprecated
    @Transactional
    public Mono<Integer> delete(AuthContext auth, int id) {
        log.info("Office Location level. Employee = {}. Id={}", auth.getUsername(), id);
        var now = dateTimeService.now();
        return secValidator.validateAdminOfficeLocation(auth)
                .flatMap(s -> repo.findById(id))
                .switchIfEmpty(Mono.error(new BusinessError("errors.entity.not.found", Integer.toString(id))))
                .flatMap(entry ->
                        history.persistHistory(entry.getId(), OFFICE_LOCATION, entry, now, auth.getEmployeeInfo().getEmployeeId())
                ).map(h -> h.getEntityId());
    }


    private Mono<Integer> doUpdate(AuthContext auth, Integer id, CreateOrUpdateOfficeLocationBody body) {
        log.info("Update office location. Employee = {}. Id={}. RequestBody = {}", auth.getUsername(), id, body);
        var now = dateTimeService.now();
        var entryToUpdate = mapper.toEntry(body);
        if (id != null) {
            entryToUpdate.setId(id);
        }
        entryToUpdate.setUpdatedAt(now);
        entryToUpdate.setUpdatedBy(auth.getEmployeeInfo().getEmployeeId());
        return secValidator.validateAdminOfficeLocation(auth)
                .flatMap((v) -> repo
                        .save(entryToUpdate)
                )
                .flatMap(entry ->
                        history.persistHistory(entry.getId(), OFFICE_LOCATION, entry, now, auth.getEmployeeInfo().getEmployeeId())
                ).map(h -> h.getEntityId());
    }


}
