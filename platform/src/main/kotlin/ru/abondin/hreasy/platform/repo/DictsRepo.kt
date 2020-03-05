package ru.abondin.hreasy.platform.repo

import org.springframework.data.annotation.Id
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono


@Table("department")
data class DepartmentEntry(
        @Id
        var id: Int?,
        var name: String
)

@Table("dict_level")
data class DictLevelEntry(
        @Id
        var id: Int?,
        var name: String,
        var weight: Int?
)

@Table("dict_position")
data class DictPositionEntry(
        @Id
        var id: Int?,
        var name: String
)

@Table("dict_vacancy_position")
data class DictVacancyPositionEntry(
        @Id
        var id: Int?,
        var name: String,
        var description: String?
)

@Table("dict_vacancy_priority")
data class DictVacancyPriorityEntry(
        @Id
        var id: Int?,
        var priority: Int?,
        var name: String
)

@Repository
interface DepartmentRepo : ReactiveCrudRepository<DepartmentEntry, Int> {
    @Query("select * from department where name=:name")
    fun findByName(name: String): Mono<DepartmentEntry>;
}


@Repository
interface DictLevelRepo : ReactiveCrudRepository<DictLevelEntry, Int> {
    @Query("select * from dict_level where name=:name")
    fun findByName(name: String): Mono<DictLevelEntry>;
}

@Repository
interface DictPositionRepo : ReactiveCrudRepository<DictPositionEntry, Int> {
    @Query("select * from dict_position where name=:name")
    fun findByName(name: String): Mono<DictPositionEntry>;
}

@Repository
interface DictVacancyPositionRepo : ReactiveCrudRepository<DictVacancyPositionEntry, Int> {
    @Query("select * from dict_vacancy_position where name=:name")
    fun findByName(name: String): Mono<DictVacancyPositionEntry>;
}

@Repository
interface DictVacancyPriorityRepo : ReactiveCrudRepository<DictVacancyPriorityEntry, Int> {
    @Query("select * from dict_vacancy_priority where name=:name")
    fun findByName(name: String): Mono<DictVacancyPriorityEntry>;
}