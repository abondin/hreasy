package ru.abondin.hreasy.platform.service.admin.dict;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.service.FileStorage;
import ru.abondin.hreasy.platform.service.dto.DeleteResourceResponse;
import ru.abondin.hreasy.platform.service.dto.UploadResponse;

/**
 * Handle office and office locations map
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminOfficeMapService {
    public static final String OFFICE_LOCATION_MAP_RESOURCE_TYPE = "office_location_map";

    private final FileStorage fileStorage;
    private final AdminDictValidator secValidator;

    public Mono<UploadResponse> uploadMap(AuthContext auth, FilePart file, long contentLength) {
        log.info("Upload new map for office location {}", file.filename());
        return secValidator.validateAdminUploadMap(auth)
                .flatMap(v -> {
                    var filename = file.filename();
                    if (fileStorage.fileExists(OFFICE_LOCATION_MAP_RESOURCE_TYPE, filename)) {
                        fileStorage.toRecycleBin(OFFICE_LOCATION_MAP_RESOURCE_TYPE, filename);
                    }
                    return fileStorage.uploadFile(OFFICE_LOCATION_MAP_RESOURCE_TYPE, filename, file, contentLength)
                            .then(Mono.just(new UploadResponse()));
                });
    }

    public Mono<DeleteResourceResponse> deleteMap(AuthContext auth, String filename) {
        log.info("Delete map for office location {}", filename);
        return secValidator.validateAdminUploadMap(auth)
                .flatMap(v -> fileStorage.toRecycleBin(OFFICE_LOCATION_MAP_RESOURCE_TYPE, filename))
                .map(DeleteResourceResponse::new);
    }

}
