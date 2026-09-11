package ru.abondin.hreasy.platform.service.allocation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.service.DateTimeService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Exports the complete authorized annual snapshot, ignoring UI search and organizational filters.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceAllocationExportService {
    private final ResourceAllocationService allocationService;
    private final ResourceAllocationExcelExporter exporter;
    private final DateTimeService dateTimeService;

    public Mono<Resource> export(int year, String unit, AuthContext auth) {
        return Mono.defer(() -> {
            boolean percentages = switch (unit) {
                case "percent" -> true;
                case "personMonths" -> false;
                default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported allocation unit");
            };
            log.info("Export allocations for year {} in {} by {}", year, unit, auth.getUsername());
            // getAnalytics enforces resource_allocation_read before loading any data.
            return allocationService.getAnalytics(year, auth).flatMap(analytics -> Mono.<Resource>fromCallable(() -> {
                try (var output = new ByteArrayOutputStream()) {
                    exporter.export(analytics, percentages, dateTimeService.now(), auth.getUsername(), output);
                    return new ByteArrayResource(output.toByteArray());
                } catch (IOException error) {
                    log.error("Unable to export allocation workbook", error);
                    throw new BusinessError("errors.export");
                }
            }).subscribeOn(Schedulers.boundedElastic()));
        });
    }
}
