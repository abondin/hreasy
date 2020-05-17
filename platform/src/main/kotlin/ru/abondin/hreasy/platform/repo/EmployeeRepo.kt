package ru.abondin.hreasy.platform.repo

import org.springframework.data.domain.Sort
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.isEquals
import org.springframework.data.r2dbc.query.Criteria
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono


@Repository
interface EmployeeRepo : ReactiveCrudRepository<EmployeeEntry, Int>, EmployeeDetailedRepo {

    @Query("select id from employee where email=:email")
    fun findIdByEmail(email: String): Mono<Int>;

    override fun <S : EmployeeEntry?> save(entity: S): Mono<S> {
        return Mono.error(NotImplementedError("Save method is deprecated. Use FullEmployeeRepo.save"));
    }

}


interface EmployeeDetailedRepo {
    fun findDetailed(criteria: Criteria?, sort: Sort): Flux<EmployeeDetailedEntry>
    fun findDetailed(id: Int): Mono<EmployeeDetailedEntry>
    fun updateCurrentProject(employeeId: Int, currentProjectId: Int): Mono<Int>
}


class EmployeeDetailedRepoImpl(private val databaseClient: DatabaseClient) : EmployeeDetailedRepo {

    override fun findDetailed(criteria: Criteria?, sort: Sort): Flux<EmployeeDetailedEntry> {
        var select = databaseClient
                .select()
                .from(EmployeeDetailedEntry::class.java);
        if (criteria != null) {
            select = select.matching(criteria);
        }
        //select.orderBy(sort);
        return select.fetch().all();
    }

    override fun findDetailed(id: Int): Mono<EmployeeDetailedEntry> {
        return databaseClient
                .select()
                .from(EmployeeDetailedEntry::class.java).matching(Criteria.where("id").isEquals(id)).fetch().one();
    }

    override fun updateCurrentProject(employeeId: Int, currentProjectId: Int): Mono<Int> {
        return databaseClient.execute("update employee set current_project=:currentProjectId where id=:employeeId")
                .bind("employeeId", employeeId)
                .bind("currentProjectId", currentProjectId).fetch().rowsUpdated();
    }
}