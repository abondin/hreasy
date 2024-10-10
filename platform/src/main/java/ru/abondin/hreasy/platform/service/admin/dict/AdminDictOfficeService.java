package ru.abondin.hreasy.platform.service.admin.dict;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.auth.AuthContext;
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
        return doUpdate(auth, null, body);
    }

    @Transactional
    public Mono<Integer> update(AuthContext auth, int id, CreateOrUpdateOfficeBody body) {
        return doUpdate(auth, id, body);
    }



    private Mono<Integer> doUpdate(AuthContext auth, Integer id, CreateOrUpdateOfficeBody body) {
        log.info("Update office. Employee = {}. Id={}. RequestBody = {}", auth.getUsername(), id, body);
        var now = dateTimeService.now();
        var entryToUpdate = mapper.toEntry(body);
        if (id != null) {
            entryToUpdate.setId(id);
        } else {
            entryToUpdate.setCreatedAt(now);
            entryToUpdate.setCreatedBy(auth.getEmployeeInfo().getEmployeeId());
        }
        return secValidator.validateAdminOffice(auth)
                .flatMap((v) -> repo
                        .save(entryToUpdate)
                )
                .flatMap(entry ->
                        history.persistHistory(entry.getId(), OFFICE, entry, now, auth.getEmployeeInfo().getEmployeeId())
                ).map(h -> h.getEntityId());
    }


}
