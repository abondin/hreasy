package ru.abondin.hreasy.platform.api.admin;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.service.admin.dict.AdminDepartmentService;
import ru.abondin.hreasy.platform.service.admin.dict.AdminDictLevelService;
import ru.abondin.hreasy.platform.service.admin.dict.AdminDictOfficeLocationService;
import ru.abondin.hreasy.platform.service.admin.dict.AdminDictPositionService;
import ru.abondin.hreasy.platform.service.admin.dict.dto.CreateOrUpdateDepartmentBody;
import ru.abondin.hreasy.platform.service.admin.dict.dto.CreateOrUpdateLevelBody;
import ru.abondin.hreasy.platform.service.admin.dict.dto.CreateOrUpdateOfficeLocationBody;
import ru.abondin.hreasy.platform.service.admin.dict.dto.CreateOrUpdatePositionBody;
import ru.abondin.hreasy.platform.service.dict.dto.DepartmentDto;
import ru.abondin.hreasy.platform.service.dict.dto.DictLevelDto;
import ru.abondin.hreasy.platform.service.dict.dto.DictOfficeLocationDto;
import ru.abondin.hreasy.platform.service.dict.dto.DictPositionDto;

@RestController()
@RequestMapping("/api/v1/admin/dict")
@RequiredArgsConstructor
@Slf4j
public class AdminDictController {

    private final AdminDepartmentService departments;
    private final AdminDictLevelService levels;
    private final AdminDictPositionService positions;
    private final AdminDictOfficeLocationService officeLocations;

    // ------------ Department CRUD
    @Operation(summary = "All departments")
    @GetMapping("/departments")
    @ResponseBody
    public Flux<DepartmentDto> departments() {
        return AuthHandler.currentAuth().flatMapMany(
                auth -> departments.findAll(auth));
    }

    @Operation(summary = "Create department")
    @PostMapping("/departments")
    @ResponseBody
    public Mono<DepartmentDto> createDepartment(CreateOrUpdateDepartmentBody body) {
        return AuthHandler.currentAuth().flatMap(
                auth -> departments.create(auth, body));
    }

    @Operation(summary = "Update department")
    @PutMapping("/departments/{id}")
    @ResponseBody
    public Mono<DepartmentDto> updateDepartment(@PathVariable int id, CreateOrUpdateDepartmentBody body) {
        return AuthHandler.currentAuth().flatMap(
                auth -> departments.update(auth, id, body));
    }

    @Operation(summary = "Delete department")
    @DeleteMapping("/departments/{id}")
    @ResponseBody
    public Mono<Integer> deleteDepartment(@PathVariable int id) {
        return AuthHandler.currentAuth().flatMap(
                auth -> departments.delete(auth, id));
    }

    // ------------ Position CRUD
    @Operation(summary = "All positions")
    @GetMapping("/positions")
    @ResponseBody
    public Flux<DictPositionDto> positions() {
        return AuthHandler.currentAuth().flatMapMany(
                auth -> positions.findAll(auth));
    }

    @Operation(summary = "Create position")
    @PostMapping("/positions")
    @ResponseBody
    public Mono<DictPositionDto> createPosition(CreateOrUpdatePositionBody body) {
        return AuthHandler.currentAuth().flatMap(
                auth -> positions.create(auth, body));
    }

    @Operation(summary = "Update position")
    @PutMapping("/positions/{id}")
    @ResponseBody
    public Mono<DictPositionDto> updatePosition(@PathVariable int id, CreateOrUpdatePositionBody body) {
        return AuthHandler.currentAuth().flatMap(
                auth -> positions.update(auth, id, body));
    }

    @Operation(summary = "Delete position")
    @DeleteMapping("/positions/{id}")
    @ResponseBody
    public Mono<Integer> deletePosition(@PathVariable int id) {
        return AuthHandler.currentAuth().flatMap(
                auth -> positions.delete(auth, id));
    }

    // ------------ Level CRUD
    @Operation(summary = "All levels")
    @GetMapping("/levels")
    @ResponseBody
    public Flux<DictLevelDto> levels() {
        return AuthHandler.currentAuth().flatMapMany(
                auth -> levels.findAll(auth));
    }

    @Operation(summary = "Create level")
    @PostMapping("/levels")
    @ResponseBody
    public Mono<DictLevelDto> createLevel(CreateOrUpdateLevelBody body) {
        return AuthHandler.currentAuth().flatMap(
                auth -> levels.create(auth, body));
    }

    @Operation(summary = "Update level")
    @PutMapping("/levels/{id}")
    @ResponseBody
    public Mono<DictLevelDto> updateLevel(@PathVariable int id, CreateOrUpdateLevelBody body) {
        return AuthHandler.currentAuth().flatMap(
                auth -> levels.update(auth, id, body));
    }

    @Operation(summary = "Delete level")
    @DeleteMapping("/levels/{id}")
    @ResponseBody
    public Mono<Integer> deleteLevel(@PathVariable int id) {
        return AuthHandler.currentAuth().flatMap(
                auth -> levels.delete(auth, id));
    }

    // ------------ Office Location CRUD
    @Operation(summary = "All office locations")
    @GetMapping("/office_locations")
    @ResponseBody
    public Flux<DictOfficeLocationDto> officeLocations() {
        return AuthHandler.currentAuth().flatMapMany(
                auth -> officeLocations.findAll(auth));
    }

    @Operation(summary = "Create level")
    @PostMapping("/office_locations")
    @ResponseBody
    public Mono<DictOfficeLocationDto> createOfficeLocation(CreateOrUpdateOfficeLocationBody body) {
        return AuthHandler.currentAuth().flatMap(
                auth -> officeLocations.create(auth, body));
    }

    @Operation(summary = "Update level")
    @PutMapping("/office_locations/{id}")
    @ResponseBody
    public Mono<DictOfficeLocationDto> updateOfficeLocation(@PathVariable int id, CreateOrUpdateOfficeLocationBody body) {
        return AuthHandler.currentAuth().flatMap(
                auth -> officeLocations.update(auth, id, body));
    }

    @Operation(summary = "Delete level")
    @DeleteMapping("/office_locations/{id}")
    @ResponseBody
    public Mono<Integer> deleteOfficeLocation(@PathVariable int id) {
        return AuthHandler.currentAuth().flatMap(
                auth -> officeLocations.delete(auth, id));
    }
}
