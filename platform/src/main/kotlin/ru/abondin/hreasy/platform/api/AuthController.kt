package ru.abondin.hreasy.platform.api

import io.swagger.v3.oas.annotations.Operation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.WebSession
import reactor.core.publisher.Mono
import ru.abondin.hreasy.platform.sec.AuthHandler
import ru.abondin.hreasy.platform.sec.CurrentUserDto
import ru.abondin.hreasy.platform.sec.LoginResponse
import ru.abondin.hreasy.platform.sec.LogoutResponse

@RestController()
@RequestMapping("/api/v1")
class AuthController {

    @Autowired
    lateinit var authHandler: AuthHandler;

    @Operation(summary = "Get information about current user from Security Context")
    @GetMapping("current-user")
    @ResponseBody
    fun currentUser(): Mono<CurrentUserDto> =
            AuthHandler.currentAuth().map(::CurrentUserDto);


    @Operation(summary = "Login in")
    @PostMapping("login")
    @ResponseBody
    fun login(@RequestBody r: LoginRequestDto, webSession: WebSession): Mono<LoginResponse> =
            authHandler.login(UsernamePasswordAuthenticationToken(r.username, r.password), webSession);

    @Operation(summary = "Logout")
    @PostMapping("logout")
    @ResponseBody
    fun logout(webSession: WebSession): Mono<LogoutResponse> =
            authHandler.logout(webSession);

    data class LoginRequestDto(
            val username: String,
            val password: String
    );
}
