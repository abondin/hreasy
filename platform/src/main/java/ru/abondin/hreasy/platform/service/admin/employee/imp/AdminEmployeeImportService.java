package ru.abondin.hreasy.platform.service.admin.employee.imp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.config.HrEasyCommonProps;
import ru.abondin.hreasy.platform.repo.employee.admin.imp.ImportWorkflowEntry;
import ru.abondin.hreasy.platform.repo.employee.admin.imp.ImportWorkflowRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.FileStorage;
import ru.abondin.hreasy.platform.service.admin.AdminSecurityValidator;
import ru.abondin.hreasy.platform.service.admin.employee.imp.dto.EmployeeImportConfig;
import ru.abondin.hreasy.platform.service.admin.employee.imp.dto.EmployeeImportMapper;
import ru.abondin.hreasy.platform.service.admin.employee.imp.dto.ImportEmployeeExcelRowDto;
import ru.abondin.hreasy.platform.service.admin.imp.ExcelImportServiceBase;
import ru.abondin.hreasy.platform.service.admin.imp.dto.ExcelImportMapperBase;
import ru.abondin.hreasy.platform.service.admin.imp.dto.ExcelImportProcessingResult;

import java.util.Locale;

@Service
@Slf4j
public class AdminEmployeeImportService extends ExcelImportServiceBase<EmployeeImportConfig, ImportEmployeeExcelRowDto> {

    private final AdminEmployeeImportProcessor importProcessor;

    private final AdminEmployeeImportCommitter committer;

    private final EmployeeImportMapper importMapper;

    private final AdminSecurityValidator validator;

    public AdminEmployeeImportService(HrEasyCommonProps props, ImportWorkflowRepo workflowRepo, FileStorage fileStorage, AdminEmployeeImportProcessor importProcessor, AdminEmployeeImportCommitter committer, DateTimeService dateTimeService, EmployeeImportMapper importMapper, AdminSecurityValidator validator) {
        super(props, workflowRepo, fileStorage, dateTimeService);
        this.importProcessor = importProcessor;
        this.committer = committer;
        this.importMapper = importMapper;
        this.validator = validator;
    }

    @Override
    protected Mono<Boolean> validateImport(AuthContext auth) {
        return validator.validateImportEmployee(auth);
    }

    @Override
    protected ExcelImportMapperBase<EmployeeImportConfig, ImportEmployeeExcelRowDto> getMapper() {
        return importMapper;
    }

    @Override
    public AdminEmployeeImportCommitter getCommitter() {
        return committer;
    }

    @Override
    protected EmployeeImportConfig defaultConfigEntry() {
        return new EmployeeImportConfig();
    }

    @Override
    protected Mono<ExcelImportProcessingResult<ImportEmployeeExcelRowDto>> processFile(AuthContext auth, EmployeeImportConfig config, Locale locale, ImportWorkflowEntry entry, Resource file) {
        return importProcessor.applyConfigAndParseExcelFile(auth, config, file, locale);
    }


}
