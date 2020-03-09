package ru.abondin.hreasy.platform.api.model

import java.time.LocalDate

data class VacationDto (
        val id: Int,
        val employee: Int,
        val emplyeeDisplayName: String,
        var year: Int,
        var startDate: LocalDate?,
        var endDate: LocalDate?,
        var notes: String?
)
