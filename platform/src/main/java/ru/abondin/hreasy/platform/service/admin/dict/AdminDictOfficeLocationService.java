package ru.abondin.hreasy.platform.service.admin.dict;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.dict.DictOfficeLocationRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.FileStorage;
import ru.abondin.hreasy.platform.service.HistoryDomainService;
import ru.abondin.hreasy.platform.service.admin.dict.dto.AdminDictDtoMapper;
import ru.abondin.hreasy.platform.service.admin.dict.dto.CreateOrUpdateOfficeLocationBody;
import ru.abondin.hreasy.platform.service.dict.dto.DictOfficeLocationDto;
import ru.abondin.hreasy.platform.service.dto.UploadResponse;

import java.io.File;

import static ru.abondin.hreasy.platform.service.HistoryDomainService.HistoryEntityType.OFFICE_LOCATION;

@RequiredArgsConstructor
@Service
@Slf4j
public class AdminDictOfficeLocationService {

    public static final String OFFICE_LOCATION_MAP_RESOURCE_TYPE = "office_location_map";

    public static String getOfficeLocationMapFileName(int officeLocationId) {
        return officeLocationId+".svg";
    }

    private final DateTimeService dateTimeService;
    private final DictOfficeLocationRepo repo;
    private final HistoryDomainService history;

    private final AdminDictDtoMapper mapper;
    private final AdminDictValidator secValidator;

    private final FileStorage fileStorage;

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


    public Mono<UploadResponse> uploadMap(AuthContext auth, int officeLocationId, FilePart file, long contentLength) {
        log.info("Upload new map for office location {}", officeLocationId);
        return secValidator.validateAdminOfficeLocation(auth)
                .flatMap(v -> {
                    var filename = getOfficeLocationMapFileName(officeLocationId);
                    if (fileStorage.fileExists(OFFICE_LOCATION_MAP_RESOURCE_TYPE, filename)){
                        fileStorage.toRecycleBin(OFFICE_LOCATION_MAP_RESOURCE_TYPE, filename);
                    }
                    return fileStorage.uploadFile(OFFICE_LOCATION_MAP_RESOURCE_TYPE, filename, file, contentLength)
                            .then(Mono.just(new UploadResponse()));
                });
    }

}

