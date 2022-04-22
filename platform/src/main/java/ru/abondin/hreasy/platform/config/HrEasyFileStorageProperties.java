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
    private Resource failbackImage = new ClassPathResource("static/noimage.png");

    private String articleAttachmentRelativePattern = "v1/fs/article/{articleId}/{fileName}";

    private String assessmentAttachmentRelativePattern = "v1/fs/assessment/{assessment}/{fileName}/{accessToken}";

    /**
     * Default time period when document can be downloaded by URL with access token
     */
    private Duration defaultDownloadAccessTokenTtl = Duration.ofHours(12);

    /**
     * Default upload content size limit in bytes
     */
    private long defaultUploadContentSizeLimitBytes = 10 * 1024 * 1024;

    /**
     * Default maximum number of files in one directory
     */
    private long defaultMaxFilesInDirectory = 10000;

}
