package ru.abondin.hreasy.platform.config.external;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.core.userdetails.User;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.abondin.hreasy.platform.auth.EmployeeBasedUserDetailsService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExternalTokenAuthenticationConverterTest {
    private static final String SYSTEM_ID = "pi-backend";
    private static final String SUBJECT = "integration.user@example.test";
    private static final String RAW_TOKEN = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";

    private ExternalTokenAuthenticationConverter converter;

    @BeforeEach
    void setUp() throws Exception {
        var token = new ExternalApiProperties.TokenConfig();
        token.setSystem(SYSTEM_ID);
        token.setSubject(SUBJECT);
        token.setSha256(sha256(RAW_TOKEN));
        var properties = new ExternalApiProperties();
        properties.setTokens(List.of(token));
        properties.validate();

        var userDetailsService = mock(EmployeeBasedUserDetailsService.class);
        converter = new ExternalTokenAuthenticationConverter(properties, userDetailsService);
        when(userDetailsService.findForExternal(SUBJECT)).thenReturn(Mono.just(
                User.withUsername(SUBJECT).password("").authorities("overtime_view").build()));
    }

    @Test
    void authenticatesTokenBoundToSystemAndActingUser() {
        StepVerifier.create(converter.convert(exchange(RAW_TOKEN)))
                .assertNext(authentication -> {
                    assertEquals(SYSTEM_ID, authentication.getDetails());
                    assertEquals(SUBJECT, authentication.getName());
                    assertTrue(authentication.getAuthorities().stream().anyMatch(authority ->
                            authority.getAuthority().equals(
                                    ExternalTokenAuthenticationConverter.EXTERNAL_API_RESERVED_AUTHORITY)));
                    assertTrue(authentication.getAuthorities().stream().anyMatch(authority ->
                            authority.getAuthority().equals("overtime_view")));
                })
                .verifyComplete();
    }

    @Test
    void rejectsUnknownToken() {
        StepVerifier.create(converter.convert(exchange(
                        "abcdef0123456789abcdef0123456789abcdef0123456789abcdef0123456789")))
                .verifyComplete();
    }

    @Test
    void rejectsMalformedToken() {
        StepVerifier.create(converter.convert(exchange("not-a-token")))
                .verifyComplete();
    }

    private MockServerWebExchange exchange(String token) {
        return MockServerWebExchange.from(MockServerHttpRequest.get("/external/api/v1/projects")
                .header("Authorization", "Bearer " + token));
    }

    private String sha256(String value) throws Exception {
        var digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(digest);
    }
}
