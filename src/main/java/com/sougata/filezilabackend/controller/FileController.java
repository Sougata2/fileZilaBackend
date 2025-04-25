package com.sougata.filezilabackend.controller;

import com.google.api.services.drive.model.File;
import com.sougata.filezilabackend.service.GoogleDriveService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@RestController
@RequiredArgsConstructor
@RequestMapping("/file")
public class FileController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final GoogleDriveService googleDriveService;

    @GetMapping("/google")
    public ResponseEntity<String> getFiles() {
        logger.info("get files");
        return ResponseEntity.ok(googleDriveService.getFiles());
    }

    @PostMapping("/google/upload")
    public ResponseEntity<String> uploadToDrive(@RequestParam(required = false) MultipartFile file) {
        logger.info("upload file to google drive");
        return ResponseEntity.ok(googleDriveService.uploadFile(file));
    }

    @GetMapping("/google/download/{id}")
    public ResponseEntity<Void> downloadFile(@PathVariable String id, HttpServletResponse response) {
        try {
            logger.info("download file with id {}", id);
            File fileMetaData = googleDriveService.downloadFile(id, response.getOutputStream());
            response.setContentType(fileMetaData.getMimeType());
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileMetaData.getName() + "\"");
            response.flushBuffer();
            return ResponseEntity.noContent().build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/google/preview/{id}")
    public void previewFile(@PathVariable String id, HttpServletResponse response) {
        try {
            logger.info("preview file with id {}", id);
            googleDriveService.previewFile(id, response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
