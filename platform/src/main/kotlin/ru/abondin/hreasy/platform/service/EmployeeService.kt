package ru.abondin.hreasy.platform.service

import org.springframework.data.domain.Sort.sort
import org.springframework.data.r2dbc.query.Criteria
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
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
        logger.debug("Find all employee from ${auth.email} account");
        //TODO Add filtering and ordering
        var criteria: Criteria? = null;
        criteria = if (includeFired) criteria else addNotFiredCriteria(criteria);
        return emplRepo.findDetailed(criteria,
                sort(EmployeeDetailedEntry::class.java)
                        .by(EmployeeDetailedEntry::lastname)
                        .and(sort(EmployeeDetailedEntry::class.java)
                                .by(EmployeeDetailedEntry::firstname))
                        .and(sort(EmployeeDetailedEntry::class.java)
                                .by(EmployeeDetailedEntry::patronymicName))
        ).map { e -> employeeEntryToDtoMap(e) };
    }

    fun addNotFiredCriteria(criteria: Criteria?): Criteria {
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
            entry.id,
            entry.lastname,
            entry.firstname,
            entry.patronymicName,
            employeeDisplayName(entry.lastname, entry.firstname, entry.patronymicName),
            entry.birthday,
            entry.sex,
            nullableDictFromNullableValues(entry.departmentId, (entry as? EmployeeDetailedEntry)?.departmentName),
            nullableDictFromNullableValues(entry.positionId, (entry as? EmployeeDetailedEntry)?.positionName),
            entry.email,
            entry.phone,
            entry.skype
    );
}

fun nullableDictFromNullableValues(id: Int?, name: String?): SimpleDictDto? = if (id == null) null else SimpleDictDto(id!!
        , name);