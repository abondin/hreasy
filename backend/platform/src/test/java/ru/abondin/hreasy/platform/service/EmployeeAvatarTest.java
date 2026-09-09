package ru.abondin.hreasy.platform.service;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.employee.EmployeeRepo;
import ru.abondin.hreasy.platform.service.currentproject.EmployeeProjectSecurityValidator;
import ru.abondin.hreasy.platform.service.mapper.EmployeeDtoMapper;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class EmployeeAvatarTest {
    private final EmployeeRepo repository = mock(EmployeeRepo.class);
    private final FileStorage storage = mock(FileStorage.class);
    private final AuthContext auth = mock(AuthContext.class);
    private final EmployeeService service = new EmployeeService(mock(EmployeeDtoMapper.class),
            mock(DateTimeService.class), repository, mock(EmployeeProjectSecurityValidator.class), storage);

    @Test
    void streamsTheSameAvatarByIdAndEmailWithoutFallback() {
        var avatar = new ByteArrayResource(new byte[]{1, 2, 3});
        when(repository.findIdForAvatar(301)).thenReturn(Mono.just(301));
        when(repository.findIdByEmailIgnoreCase("Alex.Morgan@example.test")).thenReturn(Mono.just(301));
        when(storage.streamImage("avatars", "301.png", false)).thenReturn(Mono.just(avatar));

        StepVerifier.create(service.avatar(301, auth)).expectNext(avatar).verifyComplete();
        StepVerifier.create(service.avatarByEmail(" Alex.Morgan@example.test ", auth))
                .expectNext(avatar).verifyComplete();
        verify(repository).findIdByEmailIgnoreCase("Alex.Morgan@example.test");
    }

    @Test
    void returnsNotFoundForUnknownEmployeesAndBadRequestForBlankEmail() {
        when(repository.findIdForAvatar(301)).thenReturn(Mono.empty());
        when(repository.findIdByEmailIgnoreCase("unknown@example.test")).thenReturn(Mono.empty());

        StepVerifier.create(service.avatar(301, auth))
                .expectErrorMatches(error -> error instanceof ResponseStatusException status
                        && status.getStatusCode() == HttpStatus.NOT_FOUND).verify();
        StepVerifier.create(service.avatarByEmail("unknown@example.test", auth))
                .expectErrorMatches(error -> error instanceof ResponseStatusException status
                        && status.getStatusCode() == HttpStatus.NOT_FOUND).verify();
        StepVerifier.create(service.avatarByEmail(" ", auth))
                .expectErrorMatches(error -> error instanceof ResponseStatusException status
                        && status.getStatusCode() == HttpStatus.BAD_REQUEST).verify();
        verifyNoInteractions(storage);
    }

    @Test
    void propagatesMissingAvatarInsteadOfReturningFallback() {
        when(repository.findIdByEmailIgnoreCase("alex.morgan@example.test")).thenReturn(Mono.just(301));
        when(storage.streamImage("avatars", "301.png", false))
                .thenReturn(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)));

        StepVerifier.create(service.avatarByEmail("alex.morgan@example.test", auth))
                .expectErrorMatches(error -> error instanceof ResponseStatusException status
                        && status.getStatusCode() == HttpStatus.NOT_FOUND).verify();
    }
}
