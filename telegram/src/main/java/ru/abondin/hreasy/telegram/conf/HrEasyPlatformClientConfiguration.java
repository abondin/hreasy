package ru.abondin.hreasy.telegram.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.telegram.common.JwtUtil;

@Configuration
public class HrEasyPlatformClientConfiguration {
    @Bean
    public WebClient hrEasyPlatformClient(
            HrEasyBotProps props,
            JwtUtil jwtUtil
    ) {
        return WebClient.builder()
                .baseUrl(props.getPlatform().getBaseUrl())
                .filter(authorizationHeaderFilter(jwtUtil))
                .build();
    }

    private ExchangeFilterFunction authorizationHeaderFilter(JwtUtil jwtUtil) {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest ->
                jwtUtil.getJwtTokenFromContext()
                        .map(token ->
                                ClientRequest.from(clientRequest)
                                        .header("Authorization", "Bearer " + token)
                                        .build())
                        .switchIfEmpty(Mono.error(() -> new RuntimeException("Cannot get telegram account")))
        );
    }

}
