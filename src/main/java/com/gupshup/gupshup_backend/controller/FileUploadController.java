package com.gupshup.gupshup_backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = {"http://localhost:3000", "https://gupshup-frontend.vercel.app"})
public class FileUploadController {

    // Folder jahan files save hongi
    private static final String UPLOAD_DIR = "uploads/";

    // 🛡️ SECURITY LAYER 1: Sirf inhi extensions ko permission do
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "pdf", "mp3", "webm", "m4a", "mp4"
    );

    // 🛡️ SECURITY LAYER 2: Sirf inhi MIME types ko permission do (Taaki naam badalkar virus na bheje)
    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "application/pdf",
            "audio/mpeg", "audio/webm", "audio/mp4", "video/mp4", "audio/x-m4a"
    );

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // 1. Check karo ki file khali (empty) to nahi hai
            if (file.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "File is empty"));
            }

            // File ka naam clean karo taaki path traversal attack (.../../) na ho sake
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            if (originalFilename.contains("..")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Invalid file path/name"));
            }

            // 2. File ka extension nikalo aur check karo
            String fileExtension = getFileExtension(originalFilename).toLowerCase();
            if (!ALLOWED_EXTENSIONS.contains(fileExtension)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "File type not allowed! Strictly prohibited."));
            }

            // 3. File ka andar ka Type (MIME) check karo
            String contentType = file.getContentType();
            if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "File content does not match its extension!"));
            }

            // 4. 🛡️ SECURITY LAYER 3: File ka naam hamesha ke liye badal do (UUID use karke)
            // Ab user ka diya hua naam kabhi save nahi hoga, ek random 32 character ki ID banegi
            String safeFileName = UUID.randomUUID().toString() + "." + fileExtension;

            // Folder banao agar nahi hai
            File directory = new File(UPLOAD_DIR);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // File ko safe naam ke sath save karo
            Path filePath = Paths.get(UPLOAD_DIR + safeFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Frontend ko wapas bhejne ke liye URL
            // नई लाइन
            String fileUrl = "https://gupshup-backend-81q6.onrender.com/uploads/" + safeFileName;
            
            Map<String, String> response = new HashMap<>();
            response.put("fileUrl", fileUrl);
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Server Error while uploading file."));
        }
    }

    // Ek chhota Helper Method jo file ka aakhiri extension (dot ke baad wala) nikalta hai
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}