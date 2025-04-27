package com.sougata.filezilabackend.service.impl;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.UserCredentials;
import com.sougata.filezilabackend.config.GoogleProperties;
import com.sougata.filezilabackend.service.GoogleDriveService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

@Service
@Primary
@RequiredArgsConstructor
public class DriveServiceImpl implements GoogleDriveService {

    private final GoogleProperties properties;

    /**
     * Application name.
     */
    private static final String APPLICATION_NAME = "File Upload";

    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private Drive getDrive() {
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attrs.getRequest();

            UserCredentials userCredentials = UserCredentials.newBuilder()
                    .setClientId(properties.getClientId())
                    .setClientSecret(properties.getClientSecret())
                    .setAccessToken(new AccessToken(extractAccessToken(request.getCookies()), new Date(System.currentTimeMillis() + 3600 * 1000)))
                    .setRefreshToken(extractRefreshToken(request.getCookies()))
                    .build();
            HttpCredentialsAdapter adapter = new HttpCredentialsAdapter(userCredentials);
            return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, adapter)
                    .setApplicationName(APPLICATION_NAME).build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getFiles() {
        try {
            FileList files = getDrive().files().list()
                    .setPageSize(100)
                    .execute();
            return files.getFiles().toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String uploadFile(MultipartFile file) {
        return "";
    }

    @Override
    public File downloadFile(String fileId, OutputStream outputStream) {
        return null;
    }

    @Override
    public void previewFile(String fileId, HttpServletResponse response) {

    }


    private String extractAccessToken(Cookie[] cookies) {
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("access_token")) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private String extractRefreshToken(Cookie[] cookies) {
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("refresh_token")) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
