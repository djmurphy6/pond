package com.pond.server.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.pond.server.model.User;
import com.pond.server.repository.UserRepository;
import com.pond.server.service.ImageService;
import com.pond.server.service.ImageService.ImageResult;
import com.pond.server.service.SupabaseStorage;

@RequestMapping("/uploads")
@RestController
public class ImageController {
    private final UserRepository userRepository;
    private final ImageService imageService;
    private final SupabaseStorage supabaseStorage;

    public ImageController(UserRepository userRepository, ImageService imageService, SupabaseStorage supabaseStorage) {
        this.userRepository = userRepository;
        this.imageService = imageService;
        this.supabaseStorage = supabaseStorage;
    }

    @Value("${supabase.pfp-bucket}")
    private String pfpBucket;

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadAvatar(@RequestPart("file") MultipartFile file) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        ImageResult img = imageService.process(file, 512, 512, 0.88f);

        // Prepare to delete previous file (if any) after successful upload
        String oldUrl = currentUser.getAvatar_url();
        String oldKey = null;
        if (oldUrl != null) {
            String marker = "/storage/v1/object/public/" + pfpBucket + "/";
            int idx = oldUrl.indexOf(marker);
            if (idx >= 0) {
                oldKey = oldUrl.substring(idx + marker.length());
            }
        }

        // Use a versioned key to avoid CDN/browser cache issues
        String key = "%s/%s.jpg".formatted(currentUser.getUserGU(), java.util.UUID.randomUUID().toString());
        String url = supabaseStorage.uploadPublic(pfpBucket, key, img.bytes(), img.contentType());

        currentUser.setAvatar_url(url);
        userRepository.save(currentUser);

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