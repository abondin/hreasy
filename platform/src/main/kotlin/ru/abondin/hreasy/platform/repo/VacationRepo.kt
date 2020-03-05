package ru.abondin.hreasy.platform.repo

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Table("vacation")
data class VacationEntry(
        @Id
        val id: Int,
        val employee: Int,
        var year: Int,
        @Column("start_date")
        var startDate: LocalDate?,
        @Column("end_date")
        var endDate: LocalDate?,
        var notes: String?

)

@Repository
interface VacationRepo : ReactiveCrudRepository<VacationEntry, Int> {
}