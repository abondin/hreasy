package ru.abondin.hreasy.platform.service.admin.dict;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.dict.DictOfficeWorkplaceRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.HistoryDomainService;
import ru.abondin.hreasy.platform.service.admin.dict.dto.AdminDictDtoMapper;
import ru.abondin.hreasy.platform.service.admin.dict.dto.CreateOrUpdateWorkplaceBody;
import ru.abondin.hreasy.platform.service.dict.dto.DictOfficeWorkplaceDto;

import static ru.abondin.hreasy.platform.service.HistoryDomainService.HistoryEntityType.OFFICE_WORKPLACE;

@RequiredArgsConstructor
@Service
@Slf4j
public class AdminDictOfficeWorkplaceService {

    private final DateTimeService dateTimeService;
    private final DictOfficeWorkplaceRepo repo;
    private final HistoryDomainService history;

    private final AdminDictDtoMapper mapper;
    private final AdminDictValidator secValidator;

    public Flux<DictOfficeWorkplaceDto> findAll(AuthContext auth) {
        return secValidator.validateAdminOfficeLocation(auth)
                .flatMapMany(v -> repo.findAllView())
                .map(mapper::fromEntry);
    }

    @Transactional
    public Mono<Integer> create(AuthContext auth, CreateOrUpdateWorkplaceBody body) {
        log.info("Create office workplace. Employee = {}. RequestBody = {}", auth.getUsername(), body);
        return doUpdate(auth, null, body);
    }

    @Transactional
    public Mono<Integer> update(AuthContext auth, int id, CreateOrUpdateWorkplaceBody body) {
        log.info("Update office workplace. Employee = {}. Id={}. RequestBody = {}", auth.getUsername(), id, body);
        return doUpdate(auth, id, body);
    }


    private Mono<Integer> doUpdate(AuthContext auth, Integer id, CreateOrUpdateWorkplaceBody body) {
        var now = dateTimeService.now();
        var entryToUpdate = mapper.toEntry(body);
        if (id != null) {
            entryToUpdate.setId(id);
        } else {
            entryToUpdate.setCreatedAt(now);
            entryToUpdate.setCreatedBy(auth.getEmployeeInfo().getEmployeeId());
        }
        return secValidator.validateAdminOfficeLocation(auth)
                .flatMap(v -> repo
                        .save(entryToUpdate)
                )
                .flatMap(entry ->
                        history.persistHistory(entry.getId(), OFFICE_WORKPLACE, entry, now, auth.getEmployeeInfo().getEmployeeId())
                ).map(h -> h.getEntityId());
    }


}
