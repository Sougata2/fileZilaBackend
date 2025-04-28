package com.sougata.filezilabackend.entity;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.UserCredentials;
import com.sougata.filezilabackend.config.GoogleProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;


public class DriveClass {
    private Drive drive;
    private final GoogleProperties properties;


    /**
     * Application name.
     */
    private static final String APPLICATION_NAME = "File Upload";

    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    public DriveClass(String accessToken) {
        this.properties = new GoogleProperties();
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
            this.drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, adapter)
                    .setApplicationName(APPLICATION_NAME).build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
