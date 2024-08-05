package ru.abondin.hreasy.platform.service.udr;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.udr.JuniorEntry;
import ru.abondin.hreasy.platform.repo.udr.JuniorReportEntry;

import java.util.Objects;

/**
 * Validate security rules to  update employee current project
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JuniorSecurityValidator {

    public static final String ADMIN_JUNIOR_REG_PERMISSION = "admin_junior_reg";

    public enum ViewFilter {
        /**
         * Request all juniors
         */
        ALL,
        /**
         * Request only if I mentor or if I was a mentor
         */
        MY
    }

    public Mono<ViewFilter> get(AuthContext authContext) {
        return Mono.defer(() -> {
            ViewFilter filter;
            if (authContext.getAuthorities().contains(ADMIN_JUNIOR_REG_PERMISSION)) {
                filter = ViewFilter.ALL;
            } else if (authContext.getAuthorities().contains("access_junior_reg")) {
                filter = ViewFilter.MY;
            } else {
                return null;
            }
            return Mono.just(filter);
        }).flatMap(r -> {
            if (r == null) {
                return Mono.error(new AccessDeniedException("No access to view junior registry"));
            } else {
                return Mono.just(r);
            }
        });
    }

    public Mono<Boolean> add(AuthContext auth) {
        return Mono.defer(() -> auth.getAuthorities().contains(ADMIN_JUNIOR_REG_PERMISSION)
                ? Mono.just(true) : Mono.just(false)).flatMap(r -> {
            if (Boolean.TRUE.equals(r)) {
                return Mono.just(true);
            }
            return Mono.error(new AccessDeniedException("Only user with permission admin_junior_reg can add juniors to registry"));
        });
    }

    public Mono<Boolean> delete(AuthContext auth, JuniorEntry entry) {
        return Mono.defer(() -> auth.getAuthorities().contains(ADMIN_JUNIOR_REG_PERMISSION) ||
                Objects.equals(entry.getCreatedBy(), auth.getEmployeeInfo().getEmployeeId()) ? Mono.just(true) : Mono.just(false)).flatMap(r -> {
            if (Boolean.TRUE.equals(r)) {
                return Mono.just(true);
            }
            return Mono.error(new AccessDeniedException("Only user with permission admin_junior_reg or record creator can remove juniors from registry"));
        });
    }

    public Mono<Boolean> update(AuthContext auth, JuniorEntry entry) {
        return Mono.defer(() -> auth.getAuthorities().contains(ADMIN_JUNIOR_REG_PERMISSION) ||
                Objects.equals(entry.getCreatedBy(), auth.getEmployeeInfo().getEmployeeId()) ? Mono.just(true) : Mono.just(false)).flatMap(r -> {
            if (Boolean.TRUE.equals(r)) {
                return Mono.just(true);
            }
            return Mono.error(new AccessDeniedException("Only user with permission admin_junior_reg or record creator can update juniors in registry"));
        });
    }

    public Mono<Boolean> addReport(AuthContext auth, JuniorEntry entry) {
        return Mono.defer(() ->
                        (
                auth.getAuthorities().contains(ADMIN_JUNIOR_REG_PERMISSION) ||
                Objects.equals(entry.getCreatedBy(), auth.getEmployeeInfo().getEmployeeId()) ||
                Objects.equals(entry.getMentorId(), auth.getEmployeeInfo().getEmployeeId())
                        ) ? Mono.just(true) : Mono.just(false)).flatMap(r -> {
            if (Boolean.TRUE.equals(r)) {
                return Mono.just(true);
            }
            return Mono.error(new AccessDeniedException("Only user with permission admin_junior_reg, mentor or record creator can add report to juniors in registry"));
        });
    }
    public Mono<Boolean> updateReport(AuthContext auth, JuniorEntry entry, JuniorReportEntry report) {
        return Mono.defer(() ->
                (
                        auth.getAuthorities().contains(ADMIN_JUNIOR_REG_PERMISSION) ||
                                Objects.equals(entry.getCreatedBy(), auth.getEmployeeInfo().getEmployeeId()) ||
                                Objects.equals(entry.getMentorId(), auth.getEmployeeInfo().getEmployeeId()) ||
                                Objects.equals(report.getCreatedBy(), auth.getEmployeeInfo().getEmployeeId())
                ) ? Mono.just(true) : Mono.just(false)).flatMap(r -> {
            if (Boolean.TRUE.equals(r)) {
                return Mono.just(true);
            }
            return Mono.error(new AccessDeniedException("Only user with permission admin_junior_reg, mentor or record creator can add report to juniors in registry"));
        });
    }
}
