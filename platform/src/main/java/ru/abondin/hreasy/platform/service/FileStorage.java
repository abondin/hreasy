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

    private File recycleDir;

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
     * @param resourceHome - type of the resource. <b>userAvatars</b> for example
     * @param filename     - name of the file
     */
    public Mono<Resource> streamFile(String resourceHome, String filename) {
        var resourceAbsoluteHome = new File(rootDir, resourceHome);
        validateAndCreateDir(resourceAbsoluteHome, true);
        var file = new File(resourceAbsoluteHome, filename);
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

    /**
     * Move file to .recycle folder in given path
     *
     * @param path
     * @param filename
     * @return
     */
    public Mono<Boolean> toRecycleBin(String path, String filename) {
        var file = new File(new File(rootDir, path), filename);
        var recycledFolder = new File(recycleDir, path);
        //1. Try to create folder for new recycled file
        validateAndCreateDir(recycledFolder, true);
        var recycledFile = new File(recycledFolder, filename);
        var success = false;
        if (file.isFile()) {
            // 2. Delete old file from recycled if already exists (in case of add/delete/add/delete for one file)
            if (recycledFile.isFile()) {
                boolean deleted = recycledFile.delete();
                if (!deleted){
                    log.warn("Unable to delete old file in recycle", path, filename);
                }
            }
            var renamed = file.renameTo(recycledFile);
            if (renamed) {
                success = true;
            } else {
                log.warn("Rename operation failed for {}/{}", path, filename);
            }
        } else {
            log.warn("Unable to find file {} in path {} to move to recycle bin", filename, path);
        }
        return Mono.just(success);
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
        recycleDir = new File(rootDir, ".recycle");
        validateAndCreateDir(recycleDir, props.isCreateResourceDirOnStart());
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
