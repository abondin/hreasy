package ru.abondin.hreasy.platform.service

import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import ru.abondin.hreasy.platform.config.HrEasyFileStorageProperties
import ru.abondin.hreasy.platform.logger
import java.io.File

/**
 * Upload and stream files
 */
@Service
class FileStorage : InitializingBean {

    @Autowired
    lateinit var props: HrEasyFileStorageProperties;

    lateinit var rootDir: File;


    /**
     * Stream file content
     * @param resourceType - type of the resource. <b>userAvatars</b> for example
     * @param filename - name of the file
     */
    fun streamImage(resourceType: String, filename: String, returnFailback: Boolean = true): Mono<Resource> {
        val resource: Resource;
        val resourceHome = File(rootDir, resourceType);
        validateAndCreateDir(resourceHome, true);
        val image = File(resourceHome, filename);
        if (image.exists()) {
            resource = FileSystemResource(image);
        } else {
            if (returnFailback) {
                resource = props.failbackImage;
            } else {
                return Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Image ${filename} not found. Failback image is disabled"));
            }
        }
        return Mono.just(resource);
    }


    override fun afterPropertiesSet() {
        logger().info("Initializing file storage in {}", props.resourcePath);
        rootDir = props.resourcePath.file;
        validateAndCreateDir(rootDir, props.createResourceDirOnStart);
    }


    private fun validateAndCreateDir(dir: File, autocreate: Boolean) {
        if (!dir.exists()) {
            logger().info("File storage {} does not exist", dir);
            if (autocreate) {
                dir.mkdirs();
            } else {
                throw IllegalArgumentException("Please specify correct 'hreasy.fs.resource_path' property");
            }
            validateAndCreateDir(dir, false);
        }
        if (!dir.isDirectory) {
            throw IllegalArgumentException("File Storage Dir ${dir} is not a directory");
        }
    }

}