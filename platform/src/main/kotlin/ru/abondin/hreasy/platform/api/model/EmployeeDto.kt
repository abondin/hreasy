package ru.abondin.hreasy.platform.api.model

import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDate


data class SimpleDictDto(
        val id: Int,
        val name: String?
)

/**
 * Employee information, available to any authenticated user
 */
data class EmployeeDto(
        var id: Int?,
        var lastname: String?,
        var firstname: String?,
        var patronymicName: String?,
        var birthday: LocalDate?,
        var sex: String?,
        var department: SimpleDictDto?,
        var position: SimpleDictDto?,
        var email: String?,
        var phone: String?,
        var skype: String?
)

/**
 * Full representation of Employee DTO for admin APIs.
 */
data class FullEmployeeDto(
        var id: Int?,
        var lastname: String?,
        var firstname: String?,
        var patronymicName: String?,
        var birthday: LocalDate?,
        var sex: String?,
        var dateOfEmployment: LocalDate?,
        var department: SimpleDictDto?,
        var position: SimpleDictDto?,
        var level: SimpleDictDto?,
        var workType: String?,
        var workDay: String?,
        var email: String?,
        var phone: String?,
        var skype: String?,
        var registrationAddress: String?,
        var documentSeries: String?,
        var documentNumber: String?,
        var documentIssuedBy: String?,
        var documentIssuedDate: LocalDate?,
        var foreignPassport: String?,
        var cityOfResidence: String?,
        var englishLevel: String?,
        var familyStatus: String?,
        var spouseName: String?,
        var children: String?,
        var dateOfDismissal: LocalDate?
) {
}