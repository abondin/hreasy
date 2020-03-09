package ru.abondin.hreasy.platform.repo

import org.springframework.data.annotation.Id
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
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

data class VacationView(
        val id: Int,
        val employee: Int,
        @Column("employee_firstname")
        val employeeFirstname: String?,
        @Column("employee_lastname")
        val employeeLastname: String?,
        @Column("employee_patronymic_name")
        val employeePatronymicName: String?,
        var year: Int,
        var start_date: LocalDate?,
        var end_date: LocalDate?,
        var notes: String?
)

interface VacationRepo {
    fun findAll(startsFrom: LocalDate): Flux<VacationView>;
}

@Service
class VacationRepoImpl(private val databaseClient: DatabaseClient) : VacationRepo {

    override fun findAll(startsFrom: LocalDate): Flux<VacationView> {
        var sql = "select v.*, e.firstname as employee_firstname, e.lastname as employee_lastname, e.patronymic_name as employee_patronymic_name" +
                " from vacation v inner join employee e on e.id=v.employee" +
                " where v.start_date>=:startsFrom"
        " order by v.start_date asc";
        val select = databaseClient
                .execute(sql)
                .bind("startsFrom", startsFrom)
                .`as`(VacationView::class.java);
        return select.fetch().all();
    }
}