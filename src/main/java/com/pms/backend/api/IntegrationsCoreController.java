package com.pms.backend.api;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class IntegrationsCoreController {
    private final Path uploadDir;

    public IntegrationsCoreController() {
        this.uploadDir = Paths.get(System.getProperty("user.dir")).resolve("uploads");
    }

    @PostMapping({"/integrations/Core/UploadFile", "/api/integrations/Core/UploadFile"})
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> uploadFile(@RequestPart("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "file is required");
        }

        try {
            Files.createDirectories(uploadDir);
            String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename());
            String ext = "";
            int dot = originalName.lastIndexOf('.');
            if (dot >= 0 && dot < originalName.length() - 1) {
                ext = originalName.substring(dot);
            }
            String storedName = UUID.randomUUID() + ext;
            Path target = uploadDir.resolve(storedName);
            Files.copy(file.getInputStream(), target);
            return Map.of(
                    "file_url", "/uploads/" + URLEncoder.encode(storedName, StandardCharsets.UTF_8)
            );
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Upload failed");
        }
    }
}

