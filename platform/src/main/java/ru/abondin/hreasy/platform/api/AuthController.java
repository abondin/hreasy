package ru.abondin.hreasy.platform.api;


import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.auth.AuthHandler.CurrentUserDto;

@RestController()
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthHandler authHandler;

    @Operation(summary = "Get information about current user from Security Context")
    @GetMapping("current-user")
    @ResponseBody
    public Mono<CurrentUserDto> currentUser() {
        return AuthHandler.currentAuth().map(ctx -> new CurrentUserDto(ctx));
    }


    @Operation(summary = "Login in")
    @PostMapping("login")
    @ResponseBody
    public Mono<AuthHandler.LoginResponse> login(@RequestBody LoginRequestDto r, WebSession webSession) {
        return authHandler.login(new UsernamePasswordAuthenticationToken(r.getUsername(), r.getPassword()), webSession);
    }

    @Operation(summary = "Logout")
    @PostMapping("logout")
    public Mono<AuthHandler.LogoutResponse> logout(WebSession webSession) {
        return authHandler.logout(webSession);
    }


    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class LoginRequestDto {
        private String username;
        private String password;
    }
}
