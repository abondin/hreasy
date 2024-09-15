package ru.abondin.hreasy.platform.api.admin;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.service.admin.dict.*;
import ru.abondin.hreasy.platform.service.admin.dict.dto.*;
import ru.abondin.hreasy.platform.service.dict.dto.*;

@RestController()
@RequestMapping("/api/v1/admin/dict")
@RequiredArgsConstructor
@Slf4j
public class AdminDictController {

    private final AdminDepartmentService departments;
    private final AdminDictLevelService levels;
    private final AdminDictPositionService positions;
    private final AdminDictOrganizationService organizations;
    private final AdminDictOfficeService offices;
    private final AdminDictOfficeLocationService officeLocations;

    // ------------ Department CRUD
    @Operation(summary = "All departments")
    @GetMapping("/departments")
    public Flux<DepartmentDto> departments() {
        return AuthHandler.currentAuth().flatMapMany(
                departments::findAll);
    }

    @Operation(summary = "Create department")
    @PostMapping("/departments")
    public Mono<DepartmentDto> createDepartment(@RequestBody CreateOrUpdateDepartmentBody body) {
        return AuthHandler.currentAuth().flatMap(
                auth -> departments.create(auth, body));
    }

    @Operation(summary = "Update department")
    @PutMapping("/departments/{id}")
    public Mono<DepartmentDto> updateDepartment(@PathVariable int id, @RequestBody CreateOrUpdateDepartmentBody body) {
        return AuthHandler.currentAuth().flatMap(
                auth -> departments.update(auth, id, body));
    }

    // ------------ Organization CRUD
    @Operation(summary = "All organizations")
    @GetMapping("/organizations")
    public Flux<DictOrganizationDto> organizations() {
        return AuthHandler.currentAuth().flatMapMany(
                organizations::findAll);
    }

    @Operation(summary = "Create organization")
    @PostMapping("/organizations")
    public Mono<DictOrganizationDto> createPosition(@RequestBody CreateOrUpdateOrganizationBody body) {
        return AuthHandler.currentAuth().flatMap(
                auth -> organizations.create(auth, body));
    }

    @Operation(summary = "Update organization")
    @PutMapping("/organizations/{id}")
    public Mono<DictOrganizationDto> updatePosition(@PathVariable int id, @RequestBody CreateOrUpdateOrganizationBody body) {
        return AuthHandler.currentAuth().flatMap(
                auth -> organizations.update(auth, id, body));
    }


    // ------------ Position CRUD
    @Operation(summary = "All positions")
    @GetMapping("/positions")
    public Flux<DictPositionDto> positions() {
        return AuthHandler.currentAuth().flatMapMany(
                positions::findAll);
    }

    @Operation(summary = "Create position")
    @PostMapping("/positions")
    public Mono<DictPositionDto> createPosition(@RequestBody CreateOrUpdatePositionBody body) {
        return AuthHandler.currentAuth().flatMap(
                auth -> positions.create(auth, body));
    }

    @Operation(summary = "Update position")
    @PutMapping("/positions/{id}")
    public Mono<DictPositionDto> updatePosition(@PathVariable int id, @RequestBody CreateOrUpdatePositionBody body) {
        return AuthHandler.currentAuth().flatMap(
                auth -> positions.update(auth, id, body));
    }

    // ------------ Level CRUD
    @Operation(summary = "All levels")
    @GetMapping("/levels")
    public Flux<DictLevelDto> levels() {
        return AuthHandler.currentAuth().flatMapMany(
                levels::findAll);
    }

    @Operation(summary = "Create level")
    @PostMapping("/levels")
    public Mono<DictLevelDto> createLevel(@RequestBody CreateOrUpdateLevelBody body) {
        return AuthHandler.currentAuth().flatMap(
                auth -> levels.create(auth, body));
    }

    @Operation(summary = "Update level")
    @PutMapping("/levels/{id}")
    
    public Mono<DictLevelDto> updateLevel(@PathVariable int id, @RequestBody CreateOrUpdateLevelBody body) {
        return AuthHandler.currentAuth().flatMap(
                auth -> levels.update(auth, id, body));
    }

    // ------------ Office CRUD
    @Operation(summary = "All offices")
    @GetMapping("/offices")
    public Flux<DictOfficeDto> offices() {
        return AuthHandler.currentAuth().flatMapMany(
                offices::findAll);
    }

    @Operation(summary = "Create office")
    @PostMapping("/offices")
    public Mono<Integer> createOffice(@RequestBody CreateOrUpdateOfficeBody body) {
        return AuthHandler.currentAuth().flatMap(
                auth -> offices.create(auth, body));
    }

    @Operation(summary = "Update office")
    @PutMapping("/offices/{id}")
    public Mono<Integer> updateOffice(@PathVariable int id, @RequestBody CreateOrUpdateOfficeBody body) {
        return AuthHandler.currentAuth().flatMap(
                auth -> offices.update(auth, id, body));
    }

    // ------------ Office Location CRUD
    @Operation(summary = "All office locations")
    @GetMapping("/office_locations")
    public Flux<DictOfficeLocationDto> officeLocations() {
        return AuthHandler.currentAuth().flatMapMany(
                officeLocations::findAll);
    }

    @Operation(summary = "Create office location")
    @PostMapping("/office_locations")
    public Mono<Integer> createOfficeLocation(@RequestBody CreateOrUpdateOfficeLocationBody body) {
        return AuthHandler.currentAuth().flatMap(
                auth -> officeLocations.create(auth, body));
    }

    @Operation(summary = "Update office location")
    @PutMapping("/office_locations/{id}")
    public Mono<Integer> updateOfficeLocation(@PathVariable int id, @RequestBody CreateOrUpdateOfficeLocationBody body) {
        return AuthHandler.currentAuth().flatMap(
                auth -> officeLocations.update(auth, id, body));
    }
}
