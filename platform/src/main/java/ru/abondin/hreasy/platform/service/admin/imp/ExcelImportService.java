package ru.abondin.hreasy.platform.service.admin.imp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.config.HrEasyCommonProps;
import ru.abondin.hreasy.platform.repo.employee.admin.imp.ImportWorkflowEntry;
import ru.abondin.hreasy.platform.repo.employee.admin.imp.ImportWorkflowRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.FileStorage;
import ru.abondin.hreasy.platform.service.admin.AdminSecurityValidator;
import ru.abondin.hreasy.platform.service.admin.employee.imp.AdminEmployeeImportCommitter;
import ru.abondin.hreasy.platform.service.admin.employee.imp.AdminEmployeeImportProcessor;
import ru.abondin.hreasy.platform.service.admin.employee.imp.dto.EmployeeImportConfig;
import ru.abondin.hreasy.platform.service.admin.employee.imp.dto.EmployeeImportMapper;
import ru.abondin.hreasy.platform.service.admin.employee.imp.dto.ImportEmployeeExcelRowDto;
import ru.abondin.hreasy.platform.service.admin.imp.dto.ExcelImportWorkflowDto;
import ru.abondin.hreasy.platform.service.admin.imp.dto.ExcelImportWorkflowType;

import java.io.File;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelImportService {

    public static String getImportEmployeeFolder(int employeeId, short wfType) {
        return ExcelImportWorkflowType.fromWfType(wfType).getDefaultBaseDir() + File.separator + employeeId;
    }

    private final AdminEmployeeImportProcessor importProcessor;

    private final HrEasyCommonProps props;

    private final ImportWorkflowRepo workflowRepo;

    private final AdminEmployeeImportCommitter committer;
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
    public Mono<ExcelImportWorkflowDto<EmployeeImportConfig, ImportEmployeeExcelRowDto>> getActiveOrStartNewImportProcess(AuthContext auth, ExcelImportWorkflowType wfType) {
        log.info("Get active or start new import process of type {} by {}", wfType, auth.getUsername());
        return validator.validateImportEmployee(auth).flatMap(f -> workflowRepo.get(auth.getEmployeeInfo().getEmployeeId(), wfType.getWfType())
                .switchIfEmpty(workflowRepo.save(defaultImportConfig(auth, wfType)))
                .map(importMapper::fromEntry));
    }

    /**
     * Second step in import process - upload file to process
     */
    @Transactional
    public Mono<ExcelImportWorkflowDto> uploadImportFile(AuthContext auth,
                                                         int processId,
                                                         FilePart filePart,
                                                         long contentLength) {
        log.info("Upload {} to {} import process by {}", filePart.filename(), processId, auth.getUsername());
        return validator.validateImportEmployee(auth)
                .flatMap(v -> workflowRepo.findById(processId))
                .switchIfEmpty(Mono.error(new BusinessError("errors.entity.not.found", Integer.toString(processId))))
                .flatMap(entry -> fileStorage.uploadFile(getImportEmployeeFolder(auth.getEmployeeInfo().getEmployeeId(), entry.getWfType()),
                                Integer.toString(processId), filePart, contentLength)
                        .then(Mono.defer(() -> {
                            entry.setFilename(filePart.filename());
                            entry.setFileContentLength(contentLength);
                            entry.setState(ImportWorkflowEntry.STATE_FILE_UPLOADED);
                            entry.setImportedRows(null);
                            entry.setImportProcessStats(null);
                            entry.setConfigSetAt(null);
                            entry.setConfigSetBy(null);
                            entry.setConfig(importMapper.impCfg(new EmployeeImportConfig()));
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
    public Mono<ExcelImportWorkflowDto> applyConfigAndPreview(AuthContext auth, Integer processId, EmployeeImportConfig config, Locale locale) {
        log.info("Apply configuration for {} import process by {}", processId, auth.getUsername());
        return validator.validateImportEmployee(auth)
                // 1. Get import workflow in the database
                .flatMap(v -> workflowRepo.findById(processId))
                .switchIfEmpty(Mono.error(new BusinessError("errors.entity.not.found", Integer.toString(processId))))
                // 2. Read the Excel file, stored at file system on previous step
                .flatMap(entry -> fileStorage.streamFile
                                (getImportEmployeeFolder(auth.getEmployeeInfo().getEmployeeId(), entry.getWfType()), Integer.toString(processId))
                        .flatMap(file ->
                                // 3. Process file
                                importProcessor.applyConfigAndParseExcelFile(auth, config, file, locale)
                                        // 4. Update information in the database
                                        .flatMap(processingResult -> {
                                            entry.setConfig(importMapper.impCfg(config));
                                            entry.setImportedRows(importMapper.importedRows(processingResult.getRows()));
                                            entry.setImportProcessStats(importMapper.stats(processingResult.getStats()));
                                            entry.setState(ImportWorkflowEntry.STATE_CONFIGURATION_SET);
                                            entry.setConfigSetBy(auth.getEmployeeInfo().getEmployeeId());
                                            entry.setConfigSetAt(dateTimeService.now());
                                            return workflowRepo.save(entry);
                                        }).map(importMapper::fromEntry)
                        )
                );
    }

    @Transactional
    public Mono<ExcelImportWorkflowDto> commit(AuthContext auth, Integer processId) {
        log.info("Commit import process {} by {}", processId, auth.getUsername());
        var now = dateTimeService.now();
        return validator.validateImportEmployee(auth)
                // 1. Get import workflow in the database
                .flatMap(v -> workflowRepo.findById(processId))
                .switchIfEmpty(Mono.error(new BusinessError("errors.entity.not.found", Integer.toString(processId))))
                // 2. Process each row one by one
                .flatMap(entry -> {
                    // 3. Check that import process configuration is up-to-date
                    if (entry.getConfigSetAt() == null || now.minus(props.getExcelImport().getImportConfigTtl()).isAfter(entry.getConfigSetAt())) {
                        return Mono.error(new BusinessError("errors.import.configuration_exprired"));
                    }
                    return Flux.fromIterable(importMapper.importedRows(entry.getImportedRows()))
                            .flatMap(row -> committer.commitRow(auth, row, processId))
                            .reduce(0, ((sum, updated) -> sum + updated))
                            .flatMap(updates -> {
                                log.info("Import process {} completed. Number of updates in database - {}", processId, updates);
                                entry.setCompletedAt(now);
                                entry.setCompletedBy(auth.getEmployeeInfo().getEmployeeId());
                                entry.setState(ImportWorkflowEntry.STATE_CHANGES_APPLIED);
                                return workflowRepo.save(entry);
                            });
                }).map(importMapper::fromEntry);

    }

    @Transactional
    public Mono<ExcelImportWorkflowDto> abort(AuthContext auth, Integer processId) {
        log.info("Abort import process {} by {}", processId, auth.getUsername());
        return validator.validateImportEmployee(auth)
                // 1. Get import workflow in the database
                .flatMap(v -> workflowRepo.findById(processId))
                .switchIfEmpty(Mono.error(new BusinessError("errors.entity.not.found", Integer.toString(processId))))
                // 2. Process each row one by one
                .flatMap(entry -> {
                    entry.setState(ImportWorkflowEntry.STATE_ABORTED);
                    entry.setCompletedBy(auth.getEmployeeInfo().getEmployeeId());
                    entry.setCompletedAt(dateTimeService.now());
                    return workflowRepo.save(entry);
                }).map(importMapper::fromEntry);
    }

    private ImportWorkflowEntry defaultImportConfig(AuthContext auth, ExcelImportWorkflowType wfType) {
        var entry = new ImportWorkflowEntry();
        entry.setState(ImportWorkflowEntry.STATE_CREATED);
        entry.setWfType(wfType.getWfType());
        entry.setConfig(importMapper.impCfg(new EmployeeImportConfig()));
        entry.setCreatedAt(dateTimeService.now());
        entry.setCreatedBy(auth.getEmployeeInfo().getEmployeeId());
        return entry;
    }


}
