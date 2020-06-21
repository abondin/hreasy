package ru.abondin.hreasy.platform.service

import org.springframework.data.domain.Sort
import org.springframework.data.r2dbc.query.Criteria
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.abondin.hreasy.platform.BusinessError
import ru.abondin.hreasy.platform.api.employee.EmployeeDto
import ru.abondin.hreasy.platform.api.employee.SimpleDictDto
import ru.abondin.hreasy.platform.config.AuthContext
import ru.abondin.hreasy.platform.logger
import ru.abondin.hreasy.platform.repo.EmployeeDetailedEntry
import ru.abondin.hreasy.platform.repo.EmployeeEntry
import ru.abondin.hreasy.platform.repo.EmployeeRepo

fun employeeDisplayName(lastname: String?, firstname: String?, patronymicName: String?) = listOfNotNull(lastname, firstname, patronymicName).joinToString(" ");

@Component
class EmployeeService(
        val emplRepo: EmployeeRepo,
        val dateTimeService: DateTimeService) {
    val logger = logger();

    fun findAll(auth: AuthContext, includeFired: Boolean = false): Flux<EmployeeDto> {
        logger.debug("Find all employees from ${auth.email} account");
        //TODO Add filtering and ordering
        var criteria: Criteria? = null;
        criteria = if (includeFired) criteria else addNotFiredCriteria(criteria);
        return emplRepo.findDetailed(criteria,
                Sort.by("lastname", "firstname", "patronymicName")
        ).map { e -> employeeEntryToDtoMap(e) };
    }

    fun find(employeeId: Int, auth: AuthContext): Mono<EmployeeDto> {
        logger.debug("Find {{employeeId}} employee from ${auth.email} account");
        return emplRepo.findDetailed(employeeId)
                .map { e -> employeeEntryToDtoMap(e) }
                .switchIfEmpty(Mono.error(HttpClientErrorException(HttpStatus.NOT_FOUND, "Employee with ID=${employeeId} not found")));
    }

    fun updateCurrentProject(employeeId: Int, newCurrentProjectId: Int, auth: AuthContext): Mono<Boolean> {
        logger.info("Update current project ${newCurrentProjectId} for employee ${employeeId}" +
                "by ${auth.email}");
        return emplRepo.updateCurrentProject(employeeId, newCurrentProjectId).map { updatedRowsCount -> updatedRowsCount > 0 };
    }

    fun updateCurrentProject(newCurrentProjectId: Int, auth: AuthContext): Mono<Boolean> {
        if (auth.employeeInfo == null) {
            throw BusinessError("errors.no.employee.for.auth", arrayOf(auth.email));
        }
        logger.info("Update own current project ${newCurrentProjectId} - ${auth.email} (${auth.employeeInfo.id})");
        return emplRepo.updateCurrentProject(auth.employeeInfo.id, newCurrentProjectId).map { updatedRowsCount -> updatedRowsCount > 0 };
    }

    private fun addNotFiredCriteria(criteria: Criteria?): Criteria {
        val notFiredCriteria =
                criteria?.and("date_of_dismissal") ?: Criteria.where("date_of_dismissal");

        return notFiredCriteria.isNull().or("date_of_dismissal").greaterThan(dateTimeService.now());

    }


}

/**
 * Map EmployeeEntry or EmployeeEntryDetailed to EmployeeDto
 */
fun employeeEntryToDtoMap(entry: EmployeeEntry): EmployeeDto {
    return EmployeeDto(
            id = entry.id,
            lastname = entry.lastname,
            firstname = entry.firstname,
            patronymicName = entry.patronymicName,
            displayName = employeeDisplayName(entry.lastname, entry.firstname, entry.patronymicName),
            birthday = entry.birthday,
            sex = entry.sex,
            department = nullableDictFromNullableValues(entry.departmentId, (entry as? EmployeeDetailedEntry)?.departmentName),
            currentProject = nullableDictFromNullableValues(entry.currentProjectId, (entry as? EmployeeDetailedEntry)?.currentProjectName),
            email = entry.email,
            phone = entry.phone,
            skype = entry.skype
    );
}

fun nullableDictFromNullableValues(id: Int?, name: String?): SimpleDictDto? = if (id == null) null else SimpleDictDto(id!!
        , name);