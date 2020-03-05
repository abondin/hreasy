package ru.abondin.hreasy.platform.sec

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.abondin.hreasy.platform.BusinessError
import ru.abondin.hreasy.platform.logger
import ru.abondin.hreasy.platform.repo.EmployeeRepo
import ru.abondin.hreasy.platform.repo.sec.PermissionRepo
import ru.abondin.hreasy.platform.repo.sec.UserEntry
import ru.abondin.hreasy.platform.repo.sec.UserRepo

@Component
class SecurityService(
        val userRepo: UserRepo,
        val employeeRepo: EmployeeRepo,
        val permissionRepo: PermissionRepo) {

    @Transactional
    fun getPermissions(email: String): Flux<String> {
        return getUserIdOrCreateUserFromEmployee(email)
                .flatMapMany { userId -> permissionRepo.findByUserId(userId) }
                .map { perm -> perm.permission };
    }


    /**
     * Load user from database. Create based on employee information if doesn't exists
     * @return userId if found
     */
    private fun getUserIdOrCreateUserFromEmployee(email: String): Mono<Int> {
        return userRepo.findIdByEmail(email)
                .switchIfEmpty(
                        // Create security user from employee information
                        employeeRepo.findIdByEmail(email)
                                .flatMap { employeeId -> createUser(email, employeeId) }
                                .switchIfEmpty(Mono.error(BusinessError("errors.no.employee.found", arrayOf(email))))
                )
    }

    private fun createUser(email: String, employeeId: Int): Mono<Int> {
        logger().info("Creating new security user for employee ${employeeId}:${email}")
        val userEntry = UserEntry(null, email, employeeId);
        return userRepo.save(userEntry).map { u -> u.id }
    }
}