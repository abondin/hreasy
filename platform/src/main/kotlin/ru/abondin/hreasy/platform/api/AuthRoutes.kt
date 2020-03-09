package ru.abondin.hreasy.platform.api

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.web.reactive.function.server.router
import ru.abondin.hreasy.platform.sec.AuthHandler
import ru.abondin.hreasy.platform.sec.CurrentUserDto
import java.time.Duration

/**
 * API for login/logout
 */
@Configuration
class AuthRoutes {

    /**
     * Get information about current user from Security Context
     */
    @Bean
    fun currentUserRouter() = router {
        (accept(MediaType.APPLICATION_JSON) and (hrFullUrl("current-user")))
                .nest {
                    GET("/") { req ->
                        ok().body(AuthHandler.currentAuth().map { auth -> CurrentUserDto(auth) }, CurrentUserDto::class.java);
                    }
                }
    }

    @Bean
    fun loginRouter(authHandler: AuthHandler) = router {
        (accept(MediaType.APPLICATION_JSON) and (hrFullUrl("login")))
                .nest {
                    POST("/") { req ->
                        req.bodyToMono(LoginRequestDto::class.java).zipWith(req.session())
                                .flatMap { tuple ->
                                    val r = tuple.t1;
                                    val session = tuple.t2;
                                    return@flatMap authHandler.login(UsernamePasswordAuthenticationToken(r.username, r.password), session)
                                }
                                .flatMap { response -> ok().bodyValue(response) };
                    }
                }
    }

    @Bean
    fun logout(authHandler: AuthHandler) = router {
        (accept(MediaType.APPLICATION_JSON) and (hrFullUrl("logout")))
                .nest {
                    POST("/") { req ->
                        req.session()
                                .flatMap { session ->
                                    authHandler.logout(session)
                                }.flatMap { response -> ok().bodyValue(response) };
                    }
                }
    }

}

data class LoginRequestDto(
        val username: String,
        val password: String
);





