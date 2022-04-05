package ru.abondin.hreasy.platform.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.Duration;

/**
 * Properties to support file storage
 */
@Component
@ConfigurationProperties(prefix = "hreasy.fs")
@Data
public class HrEasyFileStorageProperties {
    /**
     * Path to HR Easy File Storage Root
     */
    private Resource resourcePath = new FileSystemResource(
            new File(System.getProperty("user.dir"), ".hreasy-localdev/store"));

    /**
     * Create directory for resourcePath on startup
     */
    private boolean createResourceDirOnStart = true;

    /**
     * Image to return as failback
     */
    private Resource failbackImage = new ClassPathResource("ms/core/src/main/resources/static/noimage.png");

    private String articleAttachmentRelativePattern = "v1/fs/article/{articleId}/{fileName}";

    private String assessmentAttachmentRelativePattern = "v1/fs/assessment/{assessment}/{fileName}/{accessToken}";

    private Duration assessmentAttachmentAccessTokenTtl = Duration.ofHours(12);

}
