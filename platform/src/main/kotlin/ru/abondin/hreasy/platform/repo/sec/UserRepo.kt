package ru.abondin.hreasy.platform.repo.sec

import org.springframework.data.annotation.Id
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Table("sec_user")
data class UserEntry(
        @Id
        val id: Int?,
        val email: String,
        val employeeId: Int?
);

@Table("sec_role_perm")
data class PermissionEntry(
        @Id
        val id: Int?,
        val permission: String
)

@Repository
interface UserRepo : ReactiveCrudRepository<UserEntry, Int> {
    @Query("select u.id from sec_user u where u.email=:email")
    fun findIdByEmail(email: String): Mono<Int>;
}

@Repository
interface PermissionRepo : ReactiveCrudRepository<PermissionEntry, Int> {

    @Query("select p.* from sec_role_perm p" +
            " join sec_user_role r on p.role=r.role" +
            " where r.user_id=:userId")
    fun findByUserId(userId: Int): Flux<PermissionEntry>;
}