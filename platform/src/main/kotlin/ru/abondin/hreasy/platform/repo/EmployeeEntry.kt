package ru.abondin.hreasy.platform.repo

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDate


data class SimpleDictEntry(
        @Id
        val id: Int,
        val name: String?
)

@Table("employee")
open class EmployeeEntry {
    @Id
    var id: Int? = null;
    var lastname: String? = null;
    var firstname: String? = null;
    @Column("patronymic_name")
    var patronymicName: String? = null;
    var birthday: LocalDate? = null;
    var sex: String? = null;
    @Column("department")
    var departmentId: Int? = null;
    @Column("position")
    var positionId: Int? = null;
    var email: String? = null;
    var phone: String? = null;
    var skype: String? = null;
}

//FIXME Megre to one (maximim two entries)
@Table("employee")
open class EmployeeCreationEntry : EmployeeEntry() {
    @Column("date_of_employment")
    var dateOfEmployment: LocalDate? = null;
    @Column("level")
    var levelId: Int? = null;
    @Column("work_type")
    var workType: String? = null;
    @Column("work_day")
    var workDay: String? = null;
    @Column("registration_address")
    var registrationAddress: String? = null;
    @Column("document_series")
    var documentSeries: String? = null;
    @Column("document_number")
    var documentNumber: String? = null;
    @Column("document_issued_by")
    var documentIssuedBy: String? = null;
    @Column("document_issued_date")
    var documentIssuedDate: LocalDate? = null;
    @Column("foreign_passport")
    var foreignPassport: String? = null;
    @Column("city_of_residence")
    var cityOfResidence: String? = null;
    @Column("english_level")
    var englishLevel: String? = null;
    @Column("family_status")
    var familyStatus: String? = null;
    @Column("spouse_name")
    var spouseName: String? = null;
    var children: String? = null;
    @Column("date_of_dismissal")
    var dateOfDismissal: LocalDate? = null;
}


@Table("v_employee_detailed")
open class EmployeeDetailedEntry : EmployeeEntry() {
    var departmentName: String? = null;
    var positionName: String? = null;
    var levelName: String? = null;
}

@Table("v_employee_detailed")
open class EmployeeFullEntry : EmployeeCreationEntry() {
    var departmentName: String? = null;
    var positionName: String? = null;
    var levelName: String? = null;
}


