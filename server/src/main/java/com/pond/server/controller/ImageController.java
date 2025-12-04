package com.pond.server.controller;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pond.server.dto.UploadAvatarRequest;
import com.pond.server.model.User;
import com.pond.server.service.ImageService;
import com.pond.server.service.SupabaseStorage;
import com.pond.server.service.UserService;

/**
 * REST controller for image upload operations.
 * Handles user avatar uploads with processing and storage management.
 */
@RequestMapping("/uploads")
@RestController
public class ImageController {
    private final UserService userService;
    private final ImageService imageService;
    private final SupabaseStorage supabaseStorage;

    /**
     * Constructs a new ImageController with required dependencies.
     *
     * @param userService the service for user operations
     * @param imageService the service for image processing
     * @param supabaseStorage the service for Supabase storage operations
     */
    public ImageController(UserService userService, ImageService imageService, SupabaseStorage supabaseStorage) {
        this.userService = userService;
        this.imageService = imageService;
        this.supabaseStorage = supabaseStorage;
    }

    /**
     * Decodes a base64 image string to bytes.
     * Handles both data URLs (with prefix) and raw base64 strings.
     *
     * @param s the base64 string (with or without data URL prefix)
     * @return the decoded image bytes, or null if input is null
     */
    private byte[] decodeBase64Image(String s) {
        if (s == null) return null;
        int comma = s.indexOf(',');
        String payload = comma >= 0 ? s.substring(comma + 1) : s;
        return java.util.Base64.getDecoder().decode(payload);
    }

    @Value("${supabase.pfp-bucket}")
    private String pfpBucket;

    /**
     * Uploads and updates the authenticated user's avatar.
     * Processes the image (resize and compress), uploads to storage,
     * updates the user's avatar URL, and deletes the previous avatar.
     *
     * @param req the upload request containing base64 encoded avatar image
     * @return ResponseEntity with the new avatar URL
     */
    @PostMapping(value = "/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestBody UploadAvatarRequest req) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        // Prepare to delete previous file (if any) after successful upload
        String oldUrl = currentUser.getAvatar_url();
        String oldKey = null;
        if (oldUrl != null && !oldUrl.isBlank()) {
            String marker = "/storage/v1/object/public/" + pfpBucket + "/";
            int idx = oldUrl.indexOf(marker);
            if (idx >= 0) {
                oldKey = oldUrl.substring(idx + marker.length());
            }
        }

        // Decode base64 and process image
        byte[] raw = decodeBase64Image(req.getAvatar_base64());
        ImageService.ImageResult img = imageService.process(raw, 512, 512, 0.88f);

        // Use a versioned key to avoid CDN/browser cache issues
        String key = "%s/%s.jpg".formatted(currentUser.getUserGU(), UUID.randomUUID());
        String url = supabaseStorage.uploadPublic(pfpBucket, key, img.bytes(), img.contentType());

        userService.updateAvatar(currentUser, url);

        // Best-effort delete of the previous object
        if (oldKey != null && !oldKey.isBlank()) {
            try {
                supabaseStorage.deleteObject(pfpBucket, oldKey);
            } catch (RuntimeException e) {
                // log if you have a logger; ignore to not block response
            }
        }

        return ResponseEntity.ok(Map.of("avatar_url", url));
    }
    
}