package ru.abondin.hreasy.platform.service.admin.imp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.BusinessErrorFactory;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.config.HrEasyCommonProps;
import ru.abondin.hreasy.platform.repo.employee.admin.imp.ImportWorkflowEntry;
import ru.abondin.hreasy.platform.repo.employee.admin.imp.ImportWorkflowRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.FileStorage;
import ru.abondin.hreasy.platform.service.admin.imp.dto.*;

import java.io.File;
import java.util.Locale;

@RequiredArgsConstructor
@Slf4j
public abstract class ExcelImportServiceBase<C extends ExcelImportConfig, R extends ExcelImportRowDto> {
    public static String getImportFolder(int employeeId, short wfType) {
        return ExcelImportWorkflowType.fromWfType(wfType).getDefaultBaseDir() + File.separator + employeeId;
    }

    protected final HrEasyCommonProps props;

    protected final ImportWorkflowRepo workflowRepo;

    private final FileStorage fileStorage;

    private final DateTimeService dateTimeService;


    /**
     * First step in import process.
     *
     * @param auth
     * @return not completed or canceled process initiated by this user or start new import process
     */
    @Transactional
    public Mono<ExcelImportWorkflowDto<C, R>> getActiveOrStartNewImportProcess(AuthContext auth, ExcelImportWorkflowType wfType) {
        log.info("Get active or start new import process of type {} by {}", wfType, auth.getUsername());
        return validateImport(auth).flatMap(f -> workflowRepo.get(auth.getEmployeeInfo().getEmployeeId(), wfType.getWfType())
                .switchIfEmpty(workflowRepo.save(defaultImportConfig(auth, wfType.getWfType())))
                .map(getMapper()::fromEntry));
    }


    /**
     * Second step in import process - upload file to process
     */
    @Transactional
    public Mono<ExcelImportWorkflowDto<C, R>> uploadImportFile(AuthContext auth,
                                                               int processId,
                                                               FilePart filePart,
                                                               long contentLength) {
        log.info("Upload {} to {} import process by {}", filePart.filename(), processId, auth.getUsername());
        return validateImport(auth)
                .flatMap(v -> workflowRepo.findById(processId))
                .switchIfEmpty(BusinessErrorFactory.entityNotFound(processId))
                .flatMap(entry -> fileStorage.uploadFile(getImportFolder(auth.getEmployeeInfo().getEmployeeId(), entry.getWfType()),
                                Integer.toString(processId),
                                filePart, contentLength)
                        .then(Mono.defer(() -> {
                            entry.setFilename(filePart.filename());
                            entry.setFileContentLength(contentLength);
                            entry.setState(ImportWorkflowEntry.STATE_FILE_UPLOADED);
                            entry.setImportedRows(null);
                            entry.setImportProcessStats(null);
                            entry.setConfigSetAt(null);
                            entry.setConfigSetBy(null);
                            entry.setConfig(getMapper().impCfg(defaultConfigEntry()));
                            return workflowRepo.save(entry);
                        }))).map(getMapper()::fromEntry);
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
    public Mono<ExcelImportWorkflowDto<C, R>> applyConfigAndPreview(AuthContext auth, Integer processId, C config, Locale locale) {
        log.info("Apply configuration for {} import process by {}", processId, auth.getUsername());
        return validateImport(auth)
                // 1. Get import workflow in the database
                .flatMap(v -> workflowRepo.findById(processId))
                .switchIfEmpty(BusinessErrorFactory.entityNotFound(processId))
                // 2. Read the Excel file, stored at file system on previous step
                .flatMap(entry -> fileStorage.streamFile
                                (getImportFolder(auth.getEmployeeInfo().getEmployeeId(), entry.getWfType()), Integer.toString(processId))
                        .flatMap(file ->
                                // 3. Process file
                                processFile(auth, config, locale, entry, file)
                                        // 4. Update information in the database
                                        .flatMap(processingResult -> {
                                            entry.setConfig(getMapper().impCfg(config));
                                            entry.setImportedRows(getMapper().importedRowsToJson(processingResult.getRows()));
                                            entry.setImportProcessStats(getMapper().stats(processingResult.getStats()));
                                            entry.setState(ImportWorkflowEntry.STATE_CONFIGURATION_SET);
                                            entry.setConfigSetBy(auth.getEmployeeInfo().getEmployeeId());
                                            entry.setConfigSetAt(dateTimeService.now());
                                            return workflowRepo.save(entry);
                                        }).map(getMapper()::fromEntry)
                        )
                );
    }

    @Transactional
    public Mono<ExcelImportWorkflowDto<C, R>> commit(AuthContext auth, Integer processId) {
        log.info("Commit import process {} by {}", processId, auth.getUsername());
        var now = dateTimeService.now();
        return validateImport(auth)
                // 1. Get import workflow in the database
                .flatMap(v -> workflowRepo.findById(processId))
                .switchIfEmpty(BusinessErrorFactory.entityNotFound(processId))
                // 2. Process each row one by one
                .flatMap(entry -> {
                    // 3. Check that import process configuration is up-to-date
                    if (entry.getConfigSetAt() == null || now.minus(props.getExcelImport().getImportConfigTtl()).isAfter(entry.getConfigSetAt())) {
                        return Mono.error(new BusinessError("errors.import.configuration_exprired"));
                    }
                    return Flux.fromIterable(getMapper().importedRowsFromJson(entry.getImportedRows()))
                            .flatMap(row -> getCommitter().commitRow(auth, row, processId))
                            .reduce(0, ((sum, updated) -> sum + updated))
                            .flatMap(updates -> {
                                log.info("Import process {} completed. Number of updates in database - {}", processId, updates);
                                entry.setCompletedAt(now);
                                entry.setCompletedBy(auth.getEmployeeInfo().getEmployeeId());
                                entry.setState(ImportWorkflowEntry.STATE_CHANGES_APPLIED);
                                return workflowRepo.save(entry);
                            });
                }).map(getMapper()::fromEntry);
    }

    @Transactional
    public Mono<ExcelImportWorkflowDto<C, R>> abort(AuthContext auth, Integer processId) {
        log.info("Abort import process {} by {}", processId, auth.getUsername());
        return validateImport(auth)
                // 1. Get import workflow in the database
                .flatMap(v -> workflowRepo.findById(processId))
                .switchIfEmpty(BusinessErrorFactory.entityNotFound(processId))
                // 2. Process each row one by one
                .flatMap(entry -> {
                    entry.setState(ImportWorkflowEntry.STATE_ABORTED);
                    entry.setCompletedBy(auth.getEmployeeInfo().getEmployeeId());
                    entry.setCompletedAt(dateTimeService.now());
                    return workflowRepo.save(entry);
                }).map(getMapper()::fromEntry);
    }


    protected ImportWorkflowEntry defaultImportConfig(AuthContext auth, short wfType) {
        var entry = new ImportWorkflowEntry();
        entry.setState(ImportWorkflowEntry.STATE_CREATED);
        entry.setConfig(getMapper().impCfg(defaultConfigEntry()));
        entry.setWfType(wfType);
        entry.setCreatedAt(dateTimeService.now());
        entry.setCreatedBy(auth.getEmployeeInfo().getEmployeeId());
        return entry;
    }

    protected abstract Mono<Boolean> validateImport(AuthContext auth);

    protected abstract C defaultConfigEntry();

    protected abstract Mono<ExcelImportProcessingResult<R>> processFile(AuthContext auth, C config, Locale locale, ImportWorkflowEntry entry, Resource file);

    protected abstract ExcelImportMapperBase<C, R> getMapper();

    protected abstract ExcelImportCommitter<R> getCommitter();
}
