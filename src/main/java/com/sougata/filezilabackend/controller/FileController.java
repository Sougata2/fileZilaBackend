package com.sougata.filezilabackend.controller;

import com.google.api.services.drive.model.File;
import com.sougata.filezilabackend.service.GoogleDriveService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.Map;


@RestController
@RequiredArgsConstructor
@RequestMapping("/file")
public class FileController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final GoogleDriveService googleDriveService;

    @PostMapping("/google")
    public ResponseEntity<String> getFiles(@RequestBody Map<String, Object> request) {
        logger.info("get files");
        return ResponseEntity.ok(googleDriveService.getFiles(request.get("access_token").toString(), request.get("refresh_token").toString()));
    }

    @PostMapping("/google/upload")
    public ResponseEntity<String> uploadToDrive(@RequestParam(required = false) MultipartFile file) {
        logger.info("upload file to google drive");
        return ResponseEntity.ok(googleDriveService.uploadFile(file));
    }

    @GetMapping("/google/download/{fileId}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String fileId) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            File fileMetaData = googleDriveService.downloadFile(fileId, outputStream);
            System.out.println(fileMetaData.getName());  // Debug log to check the filename

            byte[] fileBytes = outputStream.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(fileMetaData.getMimeType()));

            // Fix: Ensure filename is correctly quoted and no special encoding needed
            String filename = fileMetaData.getName();
//            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
            headers.set("Content-Disposition", "attachment; filename=\"" + filename + "\"");

//            return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
            return ResponseEntity.ok().headers(headers).body(fileBytes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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
