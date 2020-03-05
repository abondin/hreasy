package ru.abondin.hreasy.platform.repo

import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.abondin.hreasy.platform.logger
import java.time.LocalDate

@Component()
class TestDataGenerator
(private val departmentRepo: DepartmentRepo,
 private val positionRepo: DictPositionRepo,
 private val levelRepo: DictLevelRepo,
 private val employeeRepo: EmployeeRepo
) {

    private val devDep = DepartmentEntry(null, "Development");
    private val qaDep = DepartmentEntry(null, "QA");

    private val backendDeveloperPosition = DictPositionEntry(null, "Backend Developer");
    private val frontendDeveloperPosition = DictPositionEntry(null, "Frontend Developer");
    private val manualTesterPosition = DictPositionEntry(null, "Manual Tester");

    private val juniorLevel = DictLevelEntry(null, "Junior", 10);
    private val seniorLevel = DictLevelEntry(null, "Senior", 100);

    val ivanovSeniorBackend = EmployeeCreationEntry();
    val petrovJuniorBackend = EmployeeCreationEntry();
    val sidorovSeniorFrontend = EmployeeCreationEntry();
    val alekseevFiredSeniorFrontend = EmployeeCreationEntry();
    val chackSeniorManualTester = EmployeeCreationEntry();
    val admin = EmployeeCreationEntry();

    fun generate() = Mono.zip(generateDeps(), generatePositions(), generateLevels()).flatMap { it -> generateEmployes().collectList() };

    fun generateDeps() = Mono.zip(saveDep(devDep), saveDep(qaDep));

    fun generatePositions() = Mono.zip(savePosition(backendDeveloperPosition)
            , savePosition(frontendDeveloperPosition)
            , savePosition(manualTesterPosition));


    fun generateLevels() = Mono.zip(saveLevel(juniorLevel), saveLevel(seniorLevel));

    fun generateEmployes(): Flux<EmployeeEntry> {
        ivanovSeniorBackend.departmentId = devDep.id;
        ivanovSeniorBackend.levelId = seniorLevel.id;
        ivanovSeniorBackend.positionId = backendDeveloperPosition.id;
        ivanovSeniorBackend.lastname = "Ivanov";
        ivanovSeniorBackend.firstname = "Ivan";
        ivanovSeniorBackend.patronymicName = "Ivanovich";
        withEmail(ivanovSeniorBackend);

        petrovJuniorBackend.departmentId = devDep.id;
        petrovJuniorBackend.levelId = juniorLevel.id;
        petrovJuniorBackend.positionId = backendDeveloperPosition.id;
        petrovJuniorBackend.lastname = "Petrov";
        petrovJuniorBackend.firstname = "Petr";
        petrovJuniorBackend.patronymicName = "Petrovich";
        withEmail(petrovJuniorBackend);


        sidorovSeniorFrontend.departmentId = devDep.id;
        sidorovSeniorFrontend.levelId = seniorLevel.id;
        sidorovSeniorFrontend.positionId = backendDeveloperPosition.id;
        sidorovSeniorFrontend.lastname = "Sidorov";
        sidorovSeniorFrontend.firstname = "Sidr";
        sidorovSeniorFrontend.patronymicName = null;
        withEmail(sidorovSeniorFrontend);

        alekseevFiredSeniorFrontend.departmentId = devDep.id;
        alekseevFiredSeniorFrontend.levelId = seniorLevel.id;
        alekseevFiredSeniorFrontend.positionId = backendDeveloperPosition.id;
        alekseevFiredSeniorFrontend.lastname = "Alekseev";
        alekseevFiredSeniorFrontend.firstname = "Aleksey";
        alekseevFiredSeniorFrontend.patronymicName = null;
        alekseevFiredSeniorFrontend.dateOfDismissal = LocalDate.of(2019, 12, 31);
        withEmail(alekseevFiredSeniorFrontend);


        chackSeniorManualTester.departmentId = qaDep.id;
        chackSeniorManualTester.levelId = seniorLevel.id;
        chackSeniorManualTester.positionId = manualTesterPosition.id;
        chackSeniorManualTester.lastname = "Chack";
        chackSeniorManualTester.firstname = "Chackles";
        chackSeniorManualTester.patronymicName = "Chack";
        withEmail(chackSeniorManualTester);



        admin.departmentId = devDep.id;
        admin.levelId = seniorLevel.id;
        admin.positionId = frontendDeveloperPosition.id;
        admin.lastname = "Admin";
        admin.firstname = "Admin";
        admin.patronymicName = "Admin";
        withEmail(admin);
        return Flux.fromIterable(arrayListOf(
                ivanovSeniorBackend
                , petrovJuniorBackend
                , sidorovSeniorFrontend
                , alekseevFiredSeniorFrontend
                , chackSeniorManualTester
                , admin)).flatMap { employee -> saveEmployee(employee) };

    }

    private fun saveEmployee(employee: EmployeeCreationEntry): Mono<out EmployeeCreationEntry>? {
        return employeeRepo.findIdByEmail(employee.email!!).map { id ->
            employee.id = id;
            return@map employee;
        }.switchIfEmpty(Mono.defer {
            logger().info("Creating new employee ${employee.email}")
            employeeRepo.save(employee)
        });
    }

    fun assignRoles() {

    }


    private fun withEmail(employeeEntry: EmployeeEntry): EmployeeEntry {
        employeeEntry.email = "${employeeEntry.firstname}.${employeeEntry.lastname}@stm-labs.ru";
        return employeeEntry;
    }


    private fun saveDep(department: DepartmentEntry): Mono<DepartmentEntry> =
            departmentRepo.findByName(department.name).map { d ->
                department.id = d.id
                department;
            }
                    .switchIfEmpty(departmentRepo.save(department));


    private fun savePosition(entry: DictPositionEntry): Mono<DictPositionEntry> = positionRepo.findByName(entry.name).map { e ->
        entry.id = e.id
        entry;
    }.switchIfEmpty(positionRepo.save(entry));

    private fun saveLevel(entry: DictLevelEntry): Mono<DictLevelEntry> = levelRepo.findByName(entry.name).map { e ->
        entry.id = e.id
        entry;
    }.switchIfEmpty(levelRepo.save(entry));

}