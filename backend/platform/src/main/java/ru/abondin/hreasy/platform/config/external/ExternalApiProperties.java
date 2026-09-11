package ru.abondin.hreasy.platform.config.external;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

/**
 * Hashed external API tokens bound to an external system and an acting HR Easy user.
 */
@Configuration
@ConfigurationProperties(prefix = "hreasy.external-api")
@Data
public class ExternalApiProperties {
    private List<TokenConfig> tokens = new ArrayList<>();

    @PostConstruct
    void validate() {
        var hashes = new HashSet<String>();
        for (var token : tokens) {
            Assert.hasText(token.system, "External API system must not be empty");
            Assert.hasText(token.subject, "External API subject must not be empty for system " + token.system);
            Assert.isTrue(token.sha256 != null && token.sha256.matches("[0-9a-fA-F]{64}"),
                    "External API token SHA-256 must contain 64 hexadecimal characters for system " + token.system);
            token.sha256 = token.sha256.toLowerCase();
            Assert.isTrue(hashes.add(token.sha256), "Duplicate external API token SHA-256");
        }
    }

    public Optional<TokenConfig> findToken(String sha256) {
        return tokens.stream().filter(token -> token.sha256.equals(sha256)).findFirst();
    }

    @Data
    public static class TokenConfig {
        private String system;
        private String subject;
        private String sha256;
    }
}
