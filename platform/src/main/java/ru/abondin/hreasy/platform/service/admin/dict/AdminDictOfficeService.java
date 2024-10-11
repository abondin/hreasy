package ru.abondin.hreasy.platform.service.admin.dict;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessErrorFactory;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.dict.DictOfficeEntry;
import ru.abondin.hreasy.platform.repo.dict.DictOfficeRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.HistoryDomainService;
import ru.abondin.hreasy.platform.service.admin.dict.dto.AdminDictDtoMapper;
import ru.abondin.hreasy.platform.service.admin.dict.dto.CreateOrUpdateOfficeBody;
import ru.abondin.hreasy.platform.service.dict.dto.DictOfficeDto;

import static ru.abondin.hreasy.platform.service.HistoryDomainService.HistoryEntityType.OFFICE;

@RequiredArgsConstructor
@Service
@Slf4j
public class AdminDictOfficeService {

    private final DateTimeService dateTimeService;
    private final DictOfficeRepo repo;
    private final HistoryDomainService history;

    private final AdminDictDtoMapper mapper;
    private final AdminDictValidator secValidator;

    public Flux<DictOfficeDto> findAll(AuthContext auth) {
        return secValidator.validateAdminOffice(auth)
                .flatMapMany(v -> repo.findAll())
                .map(mapper::fromEntry);
    }

    @Transactional
    public Mono<Integer> create(AuthContext auth, CreateOrUpdateOfficeBody body) {
        log.info("Create office {} by {}", body.getName(), auth.getUsername());
        var entry = new DictOfficeEntry();
        entry.setCreatedAt(dateTimeService.now());
        entry.setCreatedBy(auth.getEmployeeInfo().getEmployeeId());
        return
                doUpdate(auth, entry, body);
    }

    @Transactional
    public Mono<Integer> update(AuthContext auth, int id, CreateOrUpdateOfficeBody body) {
        log.info("Update office {} by {}", id, auth.getUsername());
        return repo.findById(id)
                .switchIfEmpty(BusinessErrorFactory.entityNotFound("Office", id))
                .flatMap(e -> doUpdate(auth, e, body));
    }


    private Mono<Integer> doUpdate(AuthContext auth, DictOfficeEntry entry, CreateOrUpdateOfficeBody body) {
        var now = dateTimeService.now();
        var entryToUpdate = mapper.applyToEntry(entry, body);
        return secValidator.validateAdminOffice(auth)
                .flatMap((v) -> repo
                        .save(entryToUpdate)
                )
                .flatMap(persisted ->
                        history.persistHistory(persisted.getId(), OFFICE, persisted, now, auth.getEmployeeInfo().getEmployeeId())
                ).map(h -> h.getEntityId());
    }


}
