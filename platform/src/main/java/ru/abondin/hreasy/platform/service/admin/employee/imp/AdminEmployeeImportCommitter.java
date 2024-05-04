package ru.abondin.hreasy.platform.service.admin.employee.imp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.service.admin.employee.AdminEmployeeService;
import ru.abondin.hreasy.platform.service.admin.employee.dto.CreateOrUpdateEmployeeBody;
import ru.abondin.hreasy.platform.service.admin.employee.dto.EmployeeAllFieldsMapper;
import ru.abondin.hreasy.platform.service.admin.employee.imp.dto.ImportEmployeeExcelRowDto;
import ru.abondin.hreasy.platform.service.admin.imp.ExcelImportCommitter;

@RequiredArgsConstructor
@Slf4j
@Service
public class AdminEmployeeImportCommitter implements ExcelImportCommitter<ImportEmployeeExcelRowDto> {
    private final AdminEmployeeService employeeService;
    private final EmployeeAllFieldsMapper mapper;


    /**
     * @param ctx
     * @param row
     * @return number of updates (actually 1 or 0 if skipped)
     */
    @Override
    public Mono<Integer> commitRow(AuthContext ctx, ImportEmployeeExcelRowDto row, Integer processId) {
        return Mono.defer(() -> {
                    // Return skip status if no cells require update
                    if (row.getUpdatedCellsCount() == 0) {
                        return Mono.just(0);
                    } else {
                        // 1. Prepare body to update
                        return createOrUpdateBody(ctx, row, processId)
                                // 2. Commit changes in database
                                .flatMap(body -> row.isNew() ? employeeService.create(ctx, body) : employeeService.update(ctx, row.getEmployeeId(), body))
                                .doOnError(e ->
                                        log.error("Unable to commit employee " + row.getEmail(), e)
                                ).map(id -> 1 // Show that row was updated
                                );
                    }
                }
        );
    }

    private Mono<CreateOrUpdateEmployeeBody> createOrUpdateBody(AuthContext ctx, ImportEmployeeExcelRowDto row, Integer processId) {
        return (row.isNew() ? Mono.just(new CreateOrUpdateEmployeeBody()) :
                employeeService.get(ctx, row.getEmployeeId())
                        .map(mapper::createOrUpdateBodyFromEntry)
        ).map(body -> {
            body.setImportProcessId(processId);
            body.setEmail(row.getEmail());
            apply(row.getDisplayName(), body::setDisplayName);
            apply(row.getExternalErpId(), body::setExtErpId);
            apply(row.getPhone(), body::setPhone);
            apply(row.getDepartment(), v -> body.setDepartmentId(v.getId()));
            apply(row.getOrganization(), v -> body.setOrganizationId(v.getId()));
            apply(row.getPosition(), v -> body.setPositionId(v.getId()));
            apply(row.getDateOfEmployment(), body::setDateOfEmployment);
            apply(row.getDateOfDismissal(), body::setDateOfDismissal);
            apply(row.getBirthday(), body::setBirthday);
            apply(row.getSex(), body::setSex);
            apply(row.getDocumentSeries(), body::setDocumentSeries);
            apply(row.getDocumentNumber(), body::setDocumentNumber);
            apply(row.getDocumentIssuedDate(), body::setDocumentIssuedDate);
            apply(row.getDocumentIssuedBy(), body::setDocumentIssuedBy);
            apply(row.getRegistrationAddress(), body::setRegistrationAddress);
            return body;
        });
    }
}
