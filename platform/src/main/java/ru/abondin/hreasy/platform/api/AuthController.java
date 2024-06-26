package ru.abondin.hreasy.platform.api;


import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.auth.AuthHandler.CurrentUserDto;
import ru.abondin.hreasy.platform.config.HrEasyCommonProps;
import ru.abondin.hreasy.platform.service.sec.AuthService;

import java.util.Locale;

@RestController()
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;
    private final HrEasyCommonProps props;

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
        var email = emailFromUserName(r.getUsername());
        return authService.login(new UsernamePasswordAuthenticationToken(email, r.getPassword()), webSession);
    }

    @Operation(summary = "Logout")
    @PostMapping("logout")
    public Mono<AuthHandler.LogoutResponse> logout(WebSession webSession) {
        return authService.logout(webSession);
    }


    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class LoginRequestDto {
        private String username;
        private String password;
    }

    /**
     * We allow user to populate username without email suffix (@company.org)
     * In that case we append email suffix automatically
     * <p>
     * Username always converts to lowercase
     *
     * @param username
     * @return
     */
    private String emailFromUserName(String username) {
        var result = username;
        if (StringUtils.isNotBlank(props.getDefaultEmailSuffix())
                && StringUtils.isNotBlank(username) && !username.contains("@")) {
            result = username + props.getDefaultEmailSuffix();
        }
        return StringUtils.trimToEmpty(result).toLowerCase(Locale.ROOT);
    }

}
