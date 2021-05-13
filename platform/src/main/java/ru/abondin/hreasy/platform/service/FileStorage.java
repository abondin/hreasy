package ru.abondin.hreasy.platform.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.config.HrEasyFileStorageProperties;

import java.io.File;
import java.io.IOException;

/**
 * Upload and stream files
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorage implements InitializingBean {
    private final HrEasyFileStorageProperties props;

    private File rootDir;

    public boolean fileExists(String resourceType, String filename) {
        var resourceHome = new File(rootDir, resourceType);
        var image = new File(resourceHome, filename);
        return image.isFile();
    }

    /**
     * Stream file content
     *
     * @param resourceType - type of the resource. <b>userAvatars</b> for example
     * @param filename     - name of the file
     */
    public Mono<Resource> streamImage(String resourceType, String filename, boolean returnFailback) {
        Resource resource;
        var resourceHome = new File(rootDir, resourceType);
        validateAndCreateDir(resourceHome, true);
        var image = new File(resourceHome, filename);
        if (image.exists()) {
            resource = new FileSystemResource(image);
        } else {
            if (returnFailback) {
                resource = props.getFailbackImage();
            } else {
                return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Image " + filename + " not found. Failback image is disabled"));
            }
        }
        return Mono.just(resource);
    }

    /**
     * Stream file content
     *
     * @param resourceType - type of the resource. <b>userAvatars</b> for example
     * @param filename     - name of the file
     */
    public Mono<Resource> streamFile(String resourceType, String filename) {
        var resourceHome = new File(rootDir, resourceType);
        validateAndCreateDir(resourceHome, true);
        var file = new File(resourceHome, filename);
        if (file.exists()) {
            return Mono.just(new FileSystemResource(file));
        } else {
            return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "File " + filename + " not found"));
        }
    }

    /**
     * Simple file list
     *
     * @param path
     * @return
     */
    public Flux<String> listFiles(String path, boolean autocreateFolder) {
        var resourceHome = new File(rootDir, path);
        validateAndCreateDir(resourceHome, autocreateFolder);
        return Flux.fromArray(resourceHome.listFiles((file) -> file.isFile())).map(File::getName).sort();
    }

    public Mono<Void> uploadFile(String resourceType, String filename, FilePart filePart) {
        log.debug("Uploading '{}' file '{}' with original file name '{}'",
                resourceType, filename, filePart.filename());
        var resourceHome = new File(rootDir, resourceType);
        validateAndCreateDir(resourceHome, true);
        return filePart.transferTo(new File(resourceHome, filename));
    }


    @Override
    public void afterPropertiesSet() throws IOException {
        log.info("Initializing file storage in {}", props.getResourcePath());
        rootDir = props.getResourcePath().getFile();
        validateAndCreateDir(rootDir, props.isCreateResourceDirOnStart());
    }


    private void validateAndCreateDir(File dir, boolean autocreate) {
        if (!dir.exists()) {
            log.info("File storage {} does not exist", dir);
            if (autocreate) {
                dir.mkdirs();
            } else {
                throw new IllegalArgumentException("Please specify correct 'hreasy.fs.resource_path' property");
            }
            validateAndCreateDir(dir, false);
        }
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException("File Storage Dir " + dir + " is not a directory");
        }
    }


}
