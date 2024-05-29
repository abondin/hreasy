package ru.abondin.hreasy.platform.config.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.EmployeeBasedUserDetailsService;
import ru.abondin.hreasy.platform.repo.employee.EmployeeAuthDomainService;

import java.util.ArrayList;
import java.util.List;

/**
 * For telegram service request authentication only
 * Populates Authentication from JWT token from Authorization header
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TelegramJwtAuthenticationConverter implements ServerAuthenticationConverter {

    /**
     * Special permission GrantedAuthority to show that employee has confirmed his telegram account
     */
    public static final String TELEGRAM_CONFIRMED_RESERVED_AUTHORITY = "__telegram_confirmed__";

    private final JwtUtil jwtUtil;


    private final EmployeeBasedUserDetailsService userDetailsService;

    private final EmployeeAuthDomainService employeeAuthDomainService;

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            String telegramAccount;
            try {
                telegramAccount = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                log.error("Invalid JWT signature", e);
                return Mono.empty();
            }
            return employeeAuthDomainService.findEmailByTelegramAccount(telegramAccount, true).flatMap(binding ->
                    userDetailsService.findForTelegram(binding.email(), telegramAccount)
                            .map(userDetails -> {
                                List<GrantedAuthority> auths = new ArrayList<>();
                                if (binding.telegramConfirmed()) {
                                    auths.add(new SimpleGrantedAuthority(TELEGRAM_CONFIRMED_RESERVED_AUTHORITY));
                                }
                                auths.addAll(userDetails.getAuthorities());
                                return new UsernamePasswordAuthenticationToken(userDetails, null, auths);
                            }));
        }
        return Mono.empty();
    }
}