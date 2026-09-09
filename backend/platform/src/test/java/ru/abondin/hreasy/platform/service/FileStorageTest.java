package ru.abondin.hreasy.platform.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.abondin.hreasy.platform.config.HrEasyFileStorageProperties;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileStorageTest {
    @TempDir
    Path directory;
    private FileStorage storage;

    @BeforeEach
    void setUp() throws Exception {
        var props = new HrEasyFileStorageProperties();
        props.setResourcePath(new FileSystemResource(directory));
        storage = new FileStorage(props);
        storage.afterPropertiesSet();
        Files.createDirectories(directory.resolve("techprofile/301"));
        Files.createDirectories(directory.resolve(".recycle/techprofile/301"));
    }

    @Test
    void deniedDeletionPreservesFileAndPreviousRecycleCopy() throws Exception {
        var source = directory.resolve("techprofile/301/profile.pdf");
        var recycled = directory.resolve(".recycle/techprofile/301/profile.pdf");
        Files.writeString(source, "current document");
        Files.writeString(recycled, "previous document");

        var deletion = storage.toRecycleBin("techprofile/301", "profile.pdf");
        assertEquals("current document", Files.readString(source));
        assertEquals("previous document", Files.readString(recycled));
        StepVerifier.create(Mono.error(new AccessDeniedException("Denied")).then(deletion))
                .expectError(AccessDeniedException.class).verify();
        assertEquals("current document", Files.readString(source));
        assertEquals("previous document", Files.readString(recycled));

        StepVerifier.create(Mono.just(true).then(deletion)).expectNext(true).verifyComplete();
        assertFalse(Files.exists(source));
        assertEquals("current document", Files.readString(recycled));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", ".", "..", "../profile.pdf", "../302/profile.pdf",
            "sub/profile.pdf", "..\\profile.pdf", "/profile.pdf", "C:\\profile.pdf", "profile.pdf:stream", "bad\0name"})
    void rejectsUnsafeNamesForEveryFileOperation(String filename) {
        var part = mock(FilePart.class);
        var folder = "techprofile/301";
        assertBadRequest(() -> storage.fileExists(folder, filename));
        assertBadRequest(() -> storage.streamImage(folder, filename, true));
        assertBadRequest(() -> storage.streamFile(folder, filename));
        assertBadRequest(() -> storage.uploadFile(folder, filename, part, 10));
        StepVerifier.create(storage.toRecycleBin(folder, filename))
                .expectErrorMatches(error -> error instanceof ResponseStatusException status
                        && status.getStatusCode().equals(HttpStatus.BAD_REQUEST)).verify();
        verify(part, never()).transferTo(any(java.io.File.class));
        verify(part, never()).transferTo(any(Path.class));
    }

    @Test
    void rejectedTraversalDoesNotOverwriteSiblingDocument() throws Exception {
        var sibling = Files.createDirectories(directory.resolve("techprofile/302")).resolve("profile.pdf");
        Files.writeString(sibling, "other employee document");
        var part = mock(FilePart.class);
        assertBadRequest(() -> storage.uploadFile("techprofile/301", "../302/profile.pdf", part, 10));
        assertEquals("other employee document", Files.readString(sibling));
    }

    @Test
    void acceptsOrdinaryNamesForUploadReadAndRecycle() throws Exception {
        var filename = "Profile 2026.pdf";
        var target = directory.resolve("techprofile/301").resolve(filename);
        var part = mock(FilePart.class);
        when(part.transferTo(target.toFile())).thenReturn(Mono.fromCallable(() ->
                Files.writeString(target, "document")).then());
        StepVerifier.create(storage.uploadFile("techprofile/301", filename, part, 10)).verifyComplete();
        assertTrue(storage.fileExists("techprofile/301", filename));
        StepVerifier.create(storage.streamFile("techprofile/301", filename))
                .assertNext(resource -> assertEquals(new FileSystemResource(target), resource)).verifyComplete();
        StepVerifier.create(storage.toRecycleBin("techprofile/301", filename)).expectNext(true).verifyComplete();
        assertFalse(storage.fileExists("techprofile/301", filename));
    }

    @Test
    void rejectsSymbolicLinkTargetsWithoutChangingTheirContent() throws Exception {
        var target = directory.resolve("other-document.pdf");
        Files.writeString(target, "protected document");
        Files.createSymbolicLink(directory.resolve("techprofile/301/link.pdf"), target);
        var part = mock(FilePart.class);
        assertBadRequest(() -> storage.uploadFile("techprofile/301", "link.pdf", part, 10));
        assertBadRequest(() -> storage.streamFile("techprofile/301", "link.pdf"));
        StepVerifier.create(storage.toRecycleBin("techprofile/301", "link.pdf"))
                .expectError(ResponseStatusException.class).verify();
        assertEquals("protected document", Files.readString(target));
    }

    private void assertBadRequest(org.junit.jupiter.api.function.Executable action) {
        var error = assertThrows(ResponseStatusException.class, action);
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
    }
}
