package ru.abondin.hreasy.platform.service.admin.employee.imp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.employee.admin.imp.ImportEmployeesWorkflowEntry;
import ru.abondin.hreasy.platform.repo.employee.admin.imp.ImportEmployeesWorkflowRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.FileStorage;
import ru.abondin.hreasy.platform.service.admin.AdminSecurityValidator;
import ru.abondin.hreasy.platform.service.admin.employee.imp.dto.EmployeeImportConfig;
import ru.abondin.hreasy.platform.service.admin.employee.imp.dto.EmployeeImportMapper;
import ru.abondin.hreasy.platform.service.admin.employee.imp.dto.ImportEmployeesWorkflowDto;

import java.io.File;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminEmployeeImportService {
    public static final String IMPORT_EMPLOYEE_BASE_DIR = "employee_import";

    public static String getImportEmployeeFolder(int employeeId) {
        return IMPORT_EMPLOYEE_BASE_DIR + File.separator + employeeId;
    }

    private final AdminEmployeeImportProcessor importProcessor;

    private final ImportEmployeesWorkflowRepo workflowRepo;

    private final DateTimeService dateTimeService;

    private final EmployeeImportMapper importMapper;


    private final FileStorage fileStorage;

    private final AdminSecurityValidator validator;


    /**
     * First step in import process.
     *
     * @param auth
     * @return not completed or canceled process initiated by this user or start new import process
     */
    @Transactional
    public Mono<ImportEmployeesWorkflowDto> getActiveOrStartNewImportProcess(AuthContext auth) {
        log.info("Get active or start new import employee process by {}", auth.getUsername());
        return validator.validateImportEmployee(auth).flatMap(f->workflowRepo.get(auth.getEmployeeInfo().getEmployeeId())
                .switchIfEmpty(workflowRepo.save(defaultImportConfig(auth)))
                .map(importMapper::fromEntry));
    }

    /**
     * Second step in import process - upload file to process
     */
    @Transactional
    public Mono<ImportEmployeesWorkflowDto> uploadImportFile(AuthContext auth,
                                                             int processId,
                                                             FilePart filePart,
                                                             long contentLength) {
        log.info("Upload {} to {} import process by {}", filePart.filename(), processId, auth.getUsername());
        return validator.validateImportEmployee(auth)
                .flatMap(v->workflowRepo.findById(processId))
                .switchIfEmpty(Mono.error(new BusinessError("errors.entity.not.found", Integer.toString(processId))))
                .flatMap(entry -> fileStorage.uploadFile(getImportEmployeeFolder(auth.getEmployeeInfo().getEmployeeId()), Integer.toString(processId), filePart, contentLength)
                        .then(Mono.defer(() -> {
                            entry.setFilename(filePart.filename());
                            entry.setFileContentLength(contentLength);
                            entry.setState(ImportEmployeesWorkflowEntry.STATE_FILE_UPLOADED);
                            entry.setData(null);
                            entry.setConfig(importMapper.config(new EmployeeImportConfig()));
                            return workflowRepo.save(entry);
                        }))).map(importMapper::fromEntry);
    }


    /**
     * Third step - apply configuration and preview results
     *
     * @param auth
     * @param processId
     * @param config
     * @return
     */
    @Transactional
    public Mono<ImportEmployeesWorkflowDto> applyConfigAndPreview(AuthContext auth, Integer processId, EmployeeImportConfig config, Locale locale) {
        return validator.validateImportEmployee(auth)
                // 1. Get import workflow in the database
                .flatMap(v->workflowRepo.findById(processId))
                .switchIfEmpty(Mono.error(new BusinessError("errors.entity.not.found", Integer.toString(processId))))
                // 2. Read the Excel file, stored at file system on previous step
                .flatMap(entry -> fileStorage.streamFile
                                (getImportEmployeeFolder(auth.getEmployeeInfo().getEmployeeId()), Integer.toString(processId))
                        .flatMap(file ->
                                // 3. Process file
                                importProcessor.applyConfigAndParseExcelFile(auth, config, file, locale)
                                        // 4. Update information in the database
                                        .flatMap(data -> {
                                            entry.setConfig(importMapper.config(config));
                                            entry.setData(importMapper.data(data));
                                            entry.setState(ImportEmployeesWorkflowEntry.STATE_CONFIGURATION_SET);
                                            return workflowRepo.save(entry);
                                        }).map(importMapper::fromEntry)
                        )
                );
    }

    private ImportEmployeesWorkflowEntry defaultImportConfig(AuthContext auth) {
        var entry = new ImportEmployeesWorkflowEntry();
        entry.setState(ImportEmployeesWorkflowEntry.STATE_CREATED);
        entry.setConfig(importMapper.config(new EmployeeImportConfig()));
        entry.setCreatedAt(dateTimeService.now());
        entry.setCreatedBy(auth.getEmployeeInfo().getEmployeeId());
        return entry;
    }
}
