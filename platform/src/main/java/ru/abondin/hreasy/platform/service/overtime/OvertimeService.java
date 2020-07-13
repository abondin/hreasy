package ru.abondin.hreasy.platform.service.overtime;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.repo.overtime.OvertimeItemRepo;
import ru.abondin.hreasy.platform.repo.overtime.OvertimeReportEntry;
import ru.abondin.hreasy.platform.repo.overtime.OvertimeReportRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.overtime.dto.NewOvertimeItemDto;
import ru.abondin.hreasy.platform.service.overtime.dto.OvertimeMapper;
import ru.abondin.hreasy.platform.service.overtime.dto.OvertimeReportDto;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class OvertimeService {
    private final OvertimeReportRepo reportRepo;
    private final OvertimeItemRepo itemRepo;
    private final OvertimeMapper mapper;
    private final DateTimeService dateTimeService;

    /**
     * Get report or empty stub to create new one
     *
     * @param employeeId
     * @param periodId
     * @return
     */
    public Mono<OvertimeReportDto> getOrStub(int employeeId, int periodId) {
        return get(employeeId, periodId).defaultIfEmpty(stub(employeeId, periodId));
    }

    /**
     * Add item to the report.
     * Create report entry if required
     *
     * @return
     */
    public Mono<OvertimeReportDto> addItem(int employeeId, int periodId, NewOvertimeItemDto newItem) {
        // 1. Get report
        return get(employeeId, periodId)
                // 2. Create entry if not exists
                .switchIfEmpty(reportRepo.save(stubEntry(employeeId, periodId))
                        .map(r -> mapper.reportToDto(r)))
                // 3. Create item
                .flatMap(report -> {
                    var itemEntry = mapper.itemToEntry(newItem);
                    var now = dateTimeService.now();
                    itemEntry.setCreatedAt(now);
                    itemEntry.setUpdatedAt(now);
                    itemEntry.setReportId(report.getId());
                    return itemRepo.save(itemEntry)
                            .map(persistedItem -> mapper.itemToDto(persistedItem))
                            .map(item -> {
                                report.getItems().add(item);
                                return report;
                            });
                });

    }


    private OvertimeReportDto stub(int employeeId, int periodId) {
        var stub = new OvertimeReportDto();
        stub.setEmployeeId(employeeId);
        stub.setPeriod(periodId);
        return stub;
    }

    private OvertimeReportEntry stubEntry(int employeeId, int periodId) {
        var stub = new OvertimeReportEntry();
        stub.setEmployeeId(employeeId);
        stub.setPeriod(periodId);
        return stub;
    }

    private Mono<OvertimeReportDto> get(int employeeId, int periodId) {
        // 1. Get report entry
        return reportRepo.get(employeeId, periodId)
                // 2. Map to DTO
                .map(r -> mapper.reportToDto(r))
                // 3. Get items
                .flatMap(report -> itemRepo.get(report.getId())
                        // 4. Map items to dto and collect to list
                        .map(item -> mapper.itemToDto(item)).collectList()
                        // 5. Return empty list if no items found
                        .defaultIfEmpty(new ArrayList<>())
                        // 6. Populate items in report
                        .map(items -> {
                            report.setItems(items);
                            return report;
                        }));
    }
}
