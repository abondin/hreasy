package ru.abondin.hreasy.platform.service.admin.employee.kids.imp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.service.admin.employee.AdminEmployeeService;
import ru.abondin.hreasy.platform.service.admin.employee.dto.CreateOrUpdateEmployeeKidBody;
import ru.abondin.hreasy.platform.service.admin.employee.dto.EmployeeAllFieldsMapper;
import ru.abondin.hreasy.platform.service.admin.employee.kids.imp.dto.ImportEmployeeKidExcelRowDto;
import ru.abondin.hreasy.platform.service.admin.imp.ExcelImportCommitter;

@RequiredArgsConstructor
@Slf4j
@Service
public class AdminEmployeeKidImportCommitter implements ExcelImportCommitter<ImportEmployeeKidExcelRowDto> {
    private final AdminEmployeeService employeeService;
    private final EmployeeAllFieldsMapper mapper;


    /**
     * @param ctx
     * @param row
     * @return number of updates (actually 1 or 0 if skipped)
     */
    @Override
    public Mono<Integer> commitRow(AuthContext ctx, ImportEmployeeKidExcelRowDto row, Integer processId) {
        return Mono.defer(() -> {
                    // Return skip status if no cells require update
                    if (row.getUpdatedCellsCount() == 0) {
                        return Mono.just(0);
                    } else {
                        // 1. Prepare body to update
                        return createOrUpdateBody(ctx, row, processId)
                                // 2. Commit changes in database
                                .flatMap(body -> row.isNew() ? employeeService.createNewKid(ctx, row.getEmployeeKidId(), body) : employeeService.updateKid(ctx, row.getParentId(), row.getEmployeeKidId(), body))
                                .doOnError(e -> {
                                    log.error("Unable to commit employee {} kid {}", row.getParentId(), row.getDisplayName(), e);
                                }).map(id -> 1 // Show that row was updated
                                );
                    }
                }
        );
    }

    private Mono<CreateOrUpdateEmployeeKidBody> createOrUpdateBody(AuthContext ctx, ImportEmployeeKidExcelRowDto row, Integer processId) {
        return (row.isNew() ? Mono.just(new CreateOrUpdateEmployeeKidBody()) :
                employeeService.getKid(ctx, row.getParentId(), row.getEmployeeKidId())
                        .map(entry -> mapper.createOrUpdateBodyFromEntry(entry))
        ).map(body -> {
            body.setImportProcessId(processId);
            body.setDisplayName(row.getDisplayName());
            apply(row.getBirthday(), v -> body.setBirthday(v));
            return body;
        });
    }




}
