package ru.abondin.hreasy.platform.service.overtime;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.overtime.*;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.overtime.dto.NewOvertimeItemDto;
import ru.abondin.hreasy.platform.service.overtime.dto.OvertimeEmployeeSummary;
import ru.abondin.hreasy.platform.service.overtime.dto.OvertimeMapper;
import ru.abondin.hreasy.platform.service.overtime.dto.OvertimeReportDto;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OvertimeService {
    private final OvertimeReportRepo reportRepo;
    private final OvertimeItemRepo itemRepo;
    private final OvertimeItemViewRepo itemViewRepo;
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
    public Mono<OvertimeReportDto> addItem(int employeeId, int periodId, NewOvertimeItemDto newItem, AuthContext ctx) {
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
                    itemEntry.setCreatedEmployeeId(ctx.getEmployeeInfo().getEmployeeId());
                    itemEntry.setReportId(report.getId());
                    return itemRepo.save(itemEntry)
                            .map(persistedItem -> mapper.itemToDto(persistedItem))
                            .map(item -> {
                                report.getItems().add(item);
                                return report;
                            });
                });
    }


    /**
     * Delete report item
     *
     * @return
     */
    public Mono<OvertimeReportDto> deleteItem(int employeeId, int periodId, int itemId, AuthContext ctx) {
        // 1. Get item
        return itemRepo.findById(itemId)
                .flatMap(item -> {
                    var now = dateTimeService.now();
                    item.setDeletedAt(now);
                    item.setDeletedEmployeeId(ctx.getEmployeeInfo().getEmployeeId());
                    return itemRepo.save(item);
                })
                .switchIfEmpty(Mono.error(new BusinessError("errors.entry.not.found", Integer.toString(itemId))))
                .then(get(employeeId, periodId));
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

    public Flux<OvertimeEmployeeSummary> getSummary(int period) {
        // Find all not deleted overtime items for given period
        return itemViewRepo.findNotDeleted(period)
                // Collect them to list. TODO - Work in flux
                .collectList().flatMapMany(list -> {
                    // Synchronous grouping items by employee
                    var groupedByEmployee = list.stream()
                            .collect(Collectors.groupingBy(OvertimeItemView::getReportEmployeeId));
                    // Map to DTO
                    var dto = groupedByEmployee
                            .entrySet().stream().map(entry -> {
                                // Convert to DTO
                                var summary = new OvertimeEmployeeSummary();
                                summary.setEmployeeId(entry.getKey());
                                summary.getItems().addAll(entry.getValue().stream().map(mapper::viewToDto).collect(Collectors.toList()));
                                return summary;
                            });
                    return Flux.fromStream(dto);
                });
    }
}
