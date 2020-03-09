package ru.abondin.hreasy.platform.api

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.queryParamOrNull
import org.springframework.web.reactive.function.server.router
import ru.abondin.hreasy.platform.sec.AuthHandler
import ru.abondin.hreasy.platform.service.EmployeeService
import ru.abondin.hreasy.platform.service.VacationService

@Configuration
class Routes() {

    @Bean
    fun employeeRouter(emplService: EmployeeService) = router {
        (accept(MediaType.APPLICATION_JSON) and (hrFullUrl("employee")))
                .nest {
                    GET("/") { req ->
                        // Get principal from session
                        AuthHandler.currentAuth()
                                // Find all employees with security filtering
                                .flatMap { ok().body(emplService.findAll(it)) }
                    }
                }
    }

    @Bean
    fun vacationRouter(service: VacationService) = router {
        (accept(MediaType.APPLICATION_JSON) and (hrFullUrl("vacation")))
                .nest {
                    GET("/") { req ->
                        ok().body(service.findAll(null))
                    }
                }
    }
}

fun hrFullUrl(url: String): String {
    return "/api/v1/${url}"
};