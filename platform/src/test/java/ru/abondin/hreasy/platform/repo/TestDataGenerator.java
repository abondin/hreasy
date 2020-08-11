package ru.abondin.hreasy.platform.repo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.repo.dict.*;
import ru.abondin.hreasy.platform.repo.employee.EmployeeCreationEntry;
import ru.abondin.hreasy.platform.repo.employee.EmployeeEntry;
import ru.abondin.hreasy.platform.repo.employee.EmployeeRepo;

import java.time.LocalDate;
import java.util.Arrays;

@Component()
@RequiredArgsConstructor
@Slf4j
public class TestDataGenerator {
    private final DepartmentRepo departmentRepo;
    private final DictPositionRepo positionRepo;
    private final DictLevelRepo levelRepo;
    private final EmployeeRepo employeeRepo;

    private DepartmentEntry devDep = new DepartmentEntry(null, "Development");
    private DepartmentEntry qaDep = new DepartmentEntry(null, "QA");

    private DictPositionEntry backendDeveloperPosition = new DictPositionEntry(null, "Backend Developer");
    private DictPositionEntry frontendDeveloperPosition = new DictPositionEntry(null, "Frontend Developer");
    private DictPositionEntry manualTesterPosition = new DictPositionEntry(null, "Manual Tester");

    private DictLevelEntry juniorLevel = new DictLevelEntry(null, "Junior", 10);
    private DictLevelEntry seniorLevel = new DictLevelEntry(null, "Senior", 100);

    EmployeeCreationEntry ivanovSeniorBackend = new EmployeeCreationEntry();
    EmployeeCreationEntry petrovJuniorBackend = new EmployeeCreationEntry();
    EmployeeCreationEntry sidorovSeniorFrontend = new EmployeeCreationEntry();
    EmployeeCreationEntry alekseevFiredSeniorFrontend = new EmployeeCreationEntry();
    EmployeeCreationEntry chackSeniorManualTester = new EmployeeCreationEntry();
    EmployeeCreationEntry admin = new EmployeeCreationEntry();

    public Mono generate() {
        return Mono.zip(generateDeps(), generatePositions(), generateLevels()).flatMap(it -> generateEmployes().collectList());
    }

    public Mono generateDeps() {
        return Mono.zip(saveDep(devDep), saveDep(qaDep));
    }

    public Mono generatePositions() {
        return Mono.zip(savePosition(backendDeveloperPosition)
                , savePosition(frontendDeveloperPosition)
                , savePosition(manualTesterPosition));
    }


    public Mono generateLevels() {
        return Mono.zip(saveLevel(juniorLevel), saveLevel(seniorLevel));
    }

    public Flux<EmployeeEntry> generateEmployes() {
        ivanovSeniorBackend.setDepartmentId(devDep.getId());
        ivanovSeniorBackend.setLevelId(seniorLevel.getId());
        ivanovSeniorBackend.setPositionId(backendDeveloperPosition.getId());
        ivanovSeniorBackend.setLastname("Ivanov");
        ivanovSeniorBackend.setFirstname("Ivan");
        ivanovSeniorBackend.setPatronymicName("Ivanovich");
        withEmail(ivanovSeniorBackend);

        petrovJuniorBackend.setDepartmentId(devDep.getId());
        petrovJuniorBackend.setLevelId(juniorLevel.getId());
        petrovJuniorBackend.setPositionId(backendDeveloperPosition.getId());
        petrovJuniorBackend.setLastname("Petrov");
        petrovJuniorBackend.setFirstname("Petr");
        petrovJuniorBackend.setPatronymicName("Petrovich");
        withEmail(petrovJuniorBackend);


        sidorovSeniorFrontend.setDepartmentId(devDep.getId());
        sidorovSeniorFrontend.setLevelId(seniorLevel.getId());
        sidorovSeniorFrontend.setPositionId(backendDeveloperPosition.getId());
        sidorovSeniorFrontend.setLastname("Sidorov");
        sidorovSeniorFrontend.setFirstname("Sidr");
        sidorovSeniorFrontend.setPatronymicName(null);
        withEmail(sidorovSeniorFrontend);

        alekseevFiredSeniorFrontend.setDepartmentId(devDep.getId());
        alekseevFiredSeniorFrontend.setLevelId(seniorLevel.getId());
        alekseevFiredSeniorFrontend.setPositionId(backendDeveloperPosition.getId());
        alekseevFiredSeniorFrontend.setLastname("Alekseev");
        alekseevFiredSeniorFrontend.setFirstname("Aleksey");
        alekseevFiredSeniorFrontend.setPatronymicName(null);
        alekseevFiredSeniorFrontend.setDateOfDismissal(LocalDate.of(2019, 12, 31));
        withEmail(alekseevFiredSeniorFrontend);


        chackSeniorManualTester.setDepartmentId(qaDep.getId());
        chackSeniorManualTester.setLevelId(seniorLevel.getId());
        chackSeniorManualTester.setPositionId(manualTesterPosition.getId());
        chackSeniorManualTester.setLastname("Chack");
        chackSeniorManualTester.setFirstname("Chackles");
        chackSeniorManualTester.setPatronymicName("Chack");
        withEmail(chackSeniorManualTester);


        admin.setDepartmentId(devDep.getId());
        admin.setLevelId(seniorLevel.getId());
        admin.setPositionId(frontendDeveloperPosition.getId());
        admin.setLastname("Admin");
        admin.setFirstname("Admin");
        admin.setPatronymicName("Admin");
        withEmail(admin);
        return Flux.fromIterable(Arrays.asList(
                ivanovSeniorBackend
                , petrovJuniorBackend
                , sidorovSeniorFrontend
                , alekseevFiredSeniorFrontend
                , chackSeniorManualTester
                , admin)).flatMap(employee -> saveEmployee(employee));

    }

    private Mono<? extends EmployeeCreationEntry> saveEmployee(EmployeeCreationEntry employee) {
        return employeeRepo.findIdByEmail(employee.getEmail()).map(id -> {
            employee.setId(id);
            return employee;
        }).switchIfEmpty(Mono.defer(() -> {
            log.info("Creating new employee ${employee.email}");
            return employeeRepo.save(employee);
        }));
    }


    private EmployeeEntry withEmail(EmployeeEntry employeeEntry) {
        employeeEntry.setEmail(employeeEntry.getFirstname() + "." + employeeEntry.getLastname() + "@stm-labs.ru");
        return employeeEntry;
    }


    private Mono<DepartmentEntry> saveDep(DepartmentEntry department) {
        return departmentRepo.findByName(department.getName()).map(d -> {
            department.setId(d.getId());
            return department;
        }).switchIfEmpty(departmentRepo.save(department));
    }


    private Mono<DictPositionEntry> savePosition(DictPositionEntry entry) {
        return positionRepo.findByName(entry.getName()).map(e -> {
                    entry.setId(e.getId());
                    return entry;
                }
        ).switchIfEmpty(positionRepo.save(entry));
    }

    private Mono<DictLevelEntry> saveLevel(DictLevelEntry entry) {
        return levelRepo.findByName(entry.getName()).map(e -> {
                    entry.setId(e.getId());
                    return entry;
                }
        ).switchIfEmpty(levelRepo.save(entry));
    }
}
