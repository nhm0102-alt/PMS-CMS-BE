package com.pms.backend.api;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class UploadsController {
    private final Path uploadDir;

    public UploadsController() {
        this.uploadDir = Paths.get(System.getProperty("user.dir")).resolve("uploads");
    }

    @GetMapping("/uploads/{fileName:.+}")
    public ResponseEntity<Resource> get(@PathVariable String fileName) {
        try {
            Path file = uploadDir.resolve(fileName).normalize();
            if (!file.startsWith(uploadDir) || !Files.exists(file) || !Files.isRegularFile(file)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found");
            }

            Resource resource = new UrlResource(file.toUri());
            String contentType = Files.probeContentType(file);
            if (contentType == null || contentType.isBlank()) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CACHE_CONTROL, "no-store")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed");
        }
    }
}

