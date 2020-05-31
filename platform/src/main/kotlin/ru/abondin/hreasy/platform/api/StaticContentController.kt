package ru.abondin.hreasy.platform.api

import io.swagger.v3.oas.annotations.Operation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.abondin.hreasy.platform.logger
import ru.abondin.hreasy.platform.sec.AuthHandler
import ru.abondin.hreasy.platform.sec.validateUploadAvatar
import ru.abondin.hreasy.platform.service.FileStorage

@RestController()
@RequestMapping("/api/v1/fs")
class StaticContentController {

    @Autowired
    lateinit var fileStorage: FileStorage;

    @Operation(summary = "Get employee avatar")
    @GetMapping(value = ["avatar/{employeeId}"], produces = arrayOf(MediaType.IMAGE_PNG_VALUE))
    fun avatar(@PathVariable employeeId: Int, @RequestParam returnFailbackImage: Boolean = false): Mono<Resource> =
            fileStorage.streamImage("avatars", "${employeeId}.png", true);

    @Operation(summary = "Upload employee avatar")
    @PostMapping(value = ["avatar/{employeeId}/upload"], produces = arrayOf(MediaType.IMAGE_PNG_VALUE))
    fun uploadAvatar(@PathVariable employeeId: Int, @RequestPart("file") multipartFile: Flux<FilePart>): Mono<String> {
        logger().debug("Upload employee avatar");
        return AuthHandler.currentAuth().flatMap { auth ->
            validateUploadAvatar(auth, employeeId);
            multipartFile.flatMap { it -> fileStorage.uploadFile("avatars", "${employeeId}.png", it) }.then(Mono.just("OK"));
        }
    }

}
