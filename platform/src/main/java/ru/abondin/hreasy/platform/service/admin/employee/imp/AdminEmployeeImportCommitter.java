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

import java.util.function.Consumer;

@RequiredArgsConstructor
@Slf4j
@Service
public class AdminEmployeeImportCommitter {
    private final AdminEmployeeService employeeService;
    private final EmployeeAllFieldsMapper mapper;


    /**
     * @param ctx
     * @param row
     * @return number of updates (actually 1 or 0 if skipped)
     */
    public Mono<Integer> commitRow(AuthContext ctx, ImportEmployeeExcelRowDto row, Integer processId) {
        return Mono.defer(() -> {
                    // Return skip status if no cells require update
                    if (row.getUpdatedCellsCount() == 0) {
                        return Mono.just(0);
                    } else {
                        // 1. Prepare body to update
                        return createOrUpdateBody(ctx, row, processId)
                                // 2. Commit changes in database
                                .flatMap(body -> row.isNew() ? employeeService.create(ctx, body) : employeeService.update(ctx, row.getEmployeeId(), body));
                    }
                }
        );
    }

    private Mono<CreateOrUpdateEmployeeBody> createOrUpdateBody(AuthContext ctx, ImportEmployeeExcelRowDto row, Integer processId) {
        return (row.isNew() ? Mono.just(new CreateOrUpdateEmployeeBody()) :
                employeeService.get(ctx, row.getEmployeeId())
                        .map(entry -> mapper.createOrUpdateBodyFromEntry(entry))
        ).map(body -> {
            body.setImportProcessId(processId);
            body.setEmail(row.getEmail());
            apply(row.getDisplayName(), v -> body.setDisplayName(v));
            apply(row.getExternalErpId(), v -> body.setExtErpId(v));
            apply(row.getPhone(), v -> body.setPhone(v));
            apply(row.getDepartment(), v -> body.setDepartmentId(v.getId()));
            apply(row.getPosition(), v -> body.setPositionId(v.getId()));
            apply(row.getDateOfEmployment(), v -> body.setDateOfEmployment(v));
            apply(row.getDateOfDismissal(), v -> body.setDateOfDismissal(v));
            apply(row.getBirthday(), v -> body.setBirthday(v));
            apply(row.getSex(), v -> body.setSex(v));
            apply(row.getDocumentSeries(), v -> body.setDocumentSeries(v));
            apply(row.getDocumentNumber(), v -> body.setDocumentNumber(v));
            apply(row.getDocumentIssuedDate(), v -> body.setDocumentIssuedDate(v));
            apply(row.getDocumentIssuedBy(), v -> body.setDocumentIssuedBy(v));
            apply(row.getRegistrationAddress(), v -> body.setRegistrationAddress(v));
            return body;
        });
    }

    private <T> void apply(ImportEmployeeExcelRowDto.DataProperty<T> cell, Consumer<T> setter) {
        if (cell.isUpdated()) {
            setter.accept(cell.getImportedValue());
        }
    }


}
