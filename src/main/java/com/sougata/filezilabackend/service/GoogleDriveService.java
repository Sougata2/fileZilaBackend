package com.sougata.filezilabackend.service;

import com.google.api.services.drive.model.File;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;

public interface GoogleDriveService {
    String getFiles();

    String uploadFile(MultipartFile file);

    File downloadFile(String fileId, OutputStream outputStream);

    void previewFile(String fileId, HttpServletResponse response);
}
