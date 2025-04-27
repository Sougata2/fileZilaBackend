package com.sougata.filezilabackend.service.impl;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.sougata.filezilabackend.service.GoogleDriveService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

/* class to demonstrate use of Drive files list API */
@Service
public class GoogleDriveServiceImpl implements GoogleDriveService {
    /**
     * Application name.
     */
    private static final String APPLICATION_NAME = "File Upload";
    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    /**
     * Directory to store authorization tokens for this application.
     */
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES =
            Collections.singletonList(DriveScopes.DRIVE_FILE);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    private static final String SERVICE_ACCOUNT_KEY_FILE = "/service-account.json"; // 🔥


    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
            throws IOException {
        // Load client secrets.
        InputStream in = GoogleDriveService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8889).build();
        //returns an authorized Credential object.
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    // use for a non-service account.
    public Drive getInstance() throws GeneralSecurityException, IOException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, new Credential(BearerToken.authorizationHeaderAccessMethod())
                .setAccessToken("<an access token>"))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    // service account has a different Google Drive set in it, so accessing that is not suitable.
    // thus useless here.
    // NOT IN USE ---
    public Drive getInstanceServiceAccount() throws GeneralSecurityException, IOException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        InputStream in = GoogleDriveServiceImpl.class.getResourceAsStream(SERVICE_ACCOUNT_KEY_FILE);
        // TODO: REPLACE THE DEPRECATED CODE
        GoogleCredential credential = GoogleCredential.fromStream(in)
                .createScoped(Collections.singleton(DriveScopes.DRIVE));

        return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
    // NOT IN USE ---


    @Override
    public String getFiles() {

        try {
            Drive service = getInstance();
            // Print the names and IDs for up to 10 files.
            FileList result = service.files().list()
                    .setQ("mimeType != 'application/vnd.google-apps.folder'") // disabling the upload file project config folder.
                    .setPageSize(100)
                    .execute();
            List<File> files = result.getFiles();
            if (files == null || files.isEmpty()) {
                System.out.println("No files found.");
                return "No files found.";
            } else {
                return files.toString();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String uploadFile(MultipartFile file) {
        try {
            System.out.println(file.getOriginalFilename());

            String folderId = "1-NQmE0gA6PmZELNeImUA4zO-CgE9Fpra";
            File fileMetadata = new File();
            fileMetadata.setParents(Collections.singletonList(folderId));
            fileMetadata.setName(file.getOriginalFilename());
            File uploadFile = getInstance()
                    .files()
                    .create(fileMetadata, new InputStreamContent(
                            file.getContentType(),
                            new ByteArrayInputStream(file.getBytes()))
                    )
                    .setFields("id").execute();
            System.out.println(uploadFile);
            return uploadFile.getId();
        } catch (Exception e) {
            System.out.printf("Error: " + e);
        }
        return null;
    }

    @Override
    public File downloadFile(String fileId, OutputStream outputStream) {
        try {
            Drive service = getInstance();
            File fileMetaData = service.files().get(fileId).setFields("name, mimeType").execute();
            service.files().get(fileId).executeMediaAndDownloadTo(outputStream);
            return fileMetaData;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void previewFile(String fileId, HttpServletResponse response) {
        try {
            Drive service = getInstance();
            File fileMetaData = service.files().get(fileId).setFields("name, mimeType").execute();

            response.setContentType(fileMetaData.getMimeType());
            response.setHeader("Content-Disposition", "inline; filename=\"" + fileMetaData.getName() + "\"");

            OutputStream outputStream = response.getOutputStream();
            service.files().get(fileId).executeMediaAndDownloadTo(outputStream);
            outputStream.flush();
        } catch (Exception e) {
            throw new RuntimeException("Failed to stream file: " + e.getMessage(), e);
        }
    }
}