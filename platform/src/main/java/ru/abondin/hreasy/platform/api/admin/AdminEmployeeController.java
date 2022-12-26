package ru.abondin.hreasy.platform.api.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.service.admin.employee.AdminEmployeeExportService;
import ru.abondin.hreasy.platform.service.admin.employee.AdminEmployeeService;
import ru.abondin.hreasy.platform.service.admin.employee.dto.*;
import ru.abondin.hreasy.platform.service.admin.employee.imp.AdminEmployeeImportService;
import ru.abondin.hreasy.platform.service.admin.employee.imp.dto.EmployeeImportConfig;
import ru.abondin.hreasy.platform.service.admin.employee.imp.dto.ImportEmployeesWorkflowDto;

import javax.validation.Valid;
import java.util.Locale;

/**
 * Add new employee record and update existing one
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/admin/employees")
public class AdminEmployeeController {

    private final AdminEmployeeService employeeService;
    private final AdminEmployeeExportService exportService;

    private final AdminEmployeeImportService importService;


    @GetMapping
    public Flux<EmployeeWithAllDetailsDto> all() {
        return AuthHandler.currentAuth().flatMapMany(auth -> employeeService.findAll(auth));
    }

    @GetMapping("/{employeeId}")
    public Mono<EmployeeWithAllDetailsDto> get(@PathVariable int employeeId) {
        return AuthHandler.currentAuth().flatMap(auth -> employeeService.get(auth, employeeId));
    }

    @PostMapping
    public Mono<Integer> create(@RequestBody @Valid CreateOrUpdateEmployeeBody body) {
        return AuthHandler.currentAuth().flatMap(auth -> employeeService.create(auth, body));
    }

    @PutMapping("/{employeeId}")
    public Mono<Integer> update(@PathVariable int employeeId, @RequestBody @Valid CreateOrUpdateEmployeeBody body) {
        return AuthHandler.currentAuth().flatMap(auth -> employeeService.update(auth, employeeId, body));
    }

    @GetMapping("/export")
    public Mono<Resource> export(Locale locale, @RequestParam(defaultValue = "false") boolean includeFired) {
        return AuthHandler.currentAuth().flatMap(auth -> exportService.export(auth,
                EmployeeExportFilter.builder()
                        .includeFired(includeFired)
                        .build()
                , locale));
    }


    @GetMapping("/kids")
    public Flux<EmployeeKidDto> allKids() {
        return AuthHandler.currentAuth().flatMapMany(auth -> employeeService.findAllKids(auth));
    }

    @PostMapping("/{employeeId}/kids")
    public Mono<Integer> createKid(@PathVariable int employeeId, @RequestBody @Valid CreateOrUpdateEmployeeKidBody body) {
        return AuthHandler.currentAuth().flatMap(auth -> employeeService.createNewKid(auth, employeeId, body));
    }

    @PutMapping("/{employeeId}/kids/{kidId}")
    public Mono<Integer> createKid(@PathVariable int employeeId, @PathVariable int kidId, @RequestBody @Valid CreateOrUpdateEmployeeKidBody body) {
        return AuthHandler.currentAuth().flatMap(auth -> employeeService.updateKid(auth, employeeId, kidId, body));
    }


    @PostMapping("/import")
    public Mono<ImportEmployeesWorkflowDto> getActiveOrStartNewImportProcess() {
        return AuthHandler.currentAuth().flatMap(auth -> importService.getActiveOrStartNewImportProcess(auth));
    }

    @PostMapping("/import/{processId}/excel")
    public Mono<ImportEmployeesWorkflowDto> uploadExcelFile(
            @PathVariable Integer processId,
            @RequestPart("file") Mono<FilePart> multipartFile,
            @RequestHeader(value = HttpHeaders.CONTENT_LENGTH, required = true) long contentLength
    ) {
        return AuthHandler.currentAuth().flatMap(auth ->
                multipartFile.flatMap(filePart -> importService.uploadImportFile(auth,
                        processId,
                        filePart,
                        contentLength)));
    }

    @PostMapping("/import/{processId}/config")
    public Mono<ImportEmployeesWorkflowDto> applyConfigAndPreview(@PathVariable Integer processId,
                                                                  @RequestBody EmployeeImportConfig config,
                                                                  Locale locale) {
        return AuthHandler.currentAuth().flatMap(auth -> importService.applyConfigAndPreview(auth, processId, config, locale));
    }

}
