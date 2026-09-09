package ru.abondin.hreasy.platform.service.allocation;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.service.DateTimeService;

import static org.mockito.Mockito.*;

class ResourceAllocationExportServiceTest {
    private final ResourceAllocationService allocations = mock(ResourceAllocationService.class);
    private final ResourceAllocationExcelExporter exporter = mock(ResourceAllocationExcelExporter.class);
    private final ResourceAllocationExportService service = new ResourceAllocationExportService(
            allocations, exporter, mock(DateTimeService.class));
    private final AuthContext auth = mock(AuthContext.class);

    @Test
    void permissionDenialNeverGeneratesAWorkbook() {
        when(allocations.getAnalytics(2026, auth)).thenReturn(Mono.error(new AccessDeniedException("Denied")));
        StepVerifier.create(service.export(2026, "personMonths", auth))
                .expectError(AccessDeniedException.class).verify();
        verifyNoInteractions(exporter);
    }

    @Test
    void rejectsUnknownUnitsBeforeLoadingData() {
        StepVerifier.create(service.export(2026, "hours", auth))
                .expectErrorMatches(error -> error instanceof ResponseStatusException status
                        && status.getStatusCode().equals(HttpStatus.BAD_REQUEST)).verify();
        verifyNoInteractions(allocations, exporter);
    }
}
