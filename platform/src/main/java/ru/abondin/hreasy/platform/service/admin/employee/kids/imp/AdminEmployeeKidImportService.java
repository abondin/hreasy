package ru.abondin.hreasy.platform.service.admin.employee.kids.imp;

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
import ru.abondin.hreasy.platform.service.admin.employee.kids.imp.dto.EmployeeKidImportConfig;
import ru.abondin.hreasy.platform.service.admin.employee.kids.imp.dto.EmployeeKidImportMapper;
import ru.abondin.hreasy.platform.service.admin.employee.kids.imp.dto.ImportEmployeeKidExcelRowDto;
import ru.abondin.hreasy.platform.service.admin.imp.ExcelImportServiceBase;
import ru.abondin.hreasy.platform.service.admin.imp.dto.ExcelImportMapperBase;
import ru.abondin.hreasy.platform.service.admin.imp.dto.ExcelImportProcessingResult;

import java.util.Locale;

@Service
@Slf4j
public class AdminEmployeeKidImportService extends ExcelImportServiceBase<EmployeeKidImportConfig, ImportEmployeeKidExcelRowDto> {

    private final AdminEmployeeKidImportProcessor importProcessor;

    private final AdminEmployeeKidImportCommitter committer;

    private final EmployeeKidImportMapper importMapper;

    private final AdminSecurityValidator validator;

    public AdminEmployeeKidImportService(HrEasyCommonProps props, ImportWorkflowRepo workflowRepo,
                                         FileStorage fileStorage,
                                         AdminEmployeeKidImportProcessor importProcessor,
                                         AdminEmployeeKidImportCommitter committer,
                                         DateTimeService dateTimeService, EmployeeKidImportMapper importMapper,
                                         AdminSecurityValidator validator) {
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
    protected ExcelImportMapperBase<EmployeeKidImportConfig, ImportEmployeeKidExcelRowDto> getMapper() {
        return importMapper;
    }

    @Override
    public AdminEmployeeKidImportCommitter getCommitter() {
        return committer;
    }

    @Override
    protected EmployeeKidImportConfig defaultConfigEntry() {
        return new EmployeeKidImportConfig();
    }

    @Override
    protected Mono<ExcelImportProcessingResult<ImportEmployeeKidExcelRowDto>> processFile(AuthContext auth,
                                                                                          EmployeeKidImportConfig config,
                                                                                          Locale locale,
                                                                                          ImportWorkflowEntry entry, Resource file) {
        return importProcessor.applyConfigAndParseExcelFile(auth, config, file, locale);
    }


}
