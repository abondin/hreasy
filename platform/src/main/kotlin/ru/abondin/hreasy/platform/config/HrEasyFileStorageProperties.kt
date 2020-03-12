package ru.abondin.hreasy.platform.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component
import java.io.File

/**
 * Properties to support file storage
 */
@Component
@ConfigurationProperties(prefix = "hreasy.fs")
class HrEasyFileStorageProperties {
    /**
     * Path to HR Easy File Storage Root
     */
    var resourcePath: Resource = FileSystemResource(File(System.getProperty("user.dir"), ".hreasy-localdev/store"));

    /**
     * Create directory for resourcePath on startup
     */
    var createResourceDirOnStart = true;

    /**
     * Image to return as failback
     */
    var failbackImage: Resource = ClassPathResource("static/noimage.png");
}