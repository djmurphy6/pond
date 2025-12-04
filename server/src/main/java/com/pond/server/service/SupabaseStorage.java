package com.pond.server.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service class for interacting with Supabase Storage API.
 * Handles file uploads and deletions in Supabase storage buckets.
 */
@Service
public class SupabaseStorage {
    @Value("${supabase.url}") private String supabaseUrl;
    @Value("${supabase.storage-url}") private String storageUrl;
    @Value("${supabase.service-role-key}") private String serviceKey;

    /**
     * Uploads a file to a public Supabase storage bucket.
     * Sets appropriate caching headers for optimal performance.
     *
     * @param bucket the name of the storage bucket
     * @param key the file path/key within the bucket
     * @param bytes the file content as bytes
     * @param contentType the MIME type of the file
     * @return the public URL of the uploaded file
     * @throws RuntimeException if upload fails
     */
    public String uploadPublic(String bucket, String key, byte[] bytes, String contentType) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(storageUrl + "/storage/v1/object/" + bucket + "/" + key))  // Change line 19: use storageUrl instead
                .header("Authorization", "Bearer " + serviceKey)
                .header("Content-Type", contentType)
                .header("Cache-Control", "public, max-age=31536000, immutable")
                .PUT(HttpRequest.BodyPublishers.ofByteArray(bytes))
                .build();
            HttpResponse<Void> resp = HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.discarding());
            if (resp.statusCode() >= 300) throw new RuntimeException("Upload failed: " + resp.statusCode());
            return storageUrl + "/storage/v1/object/public/" + bucket + "/" + key; // Also update line 27 to use storageUrl
        } catch (Exception e) {
            throw new RuntimeException("Supabase upload failed", e);
        }
    }

    /**
     * Deletes a file from a Supabase storage bucket.
     * Logs success/failure for debugging purposes.
     *
     * @param bucket the name of the storage bucket
     * @param key the file path/key within the bucket
     * @throws RuntimeException if deletion fails
     */
    public void deleteObject(String bucket, String key){
        String deleteUrl = storageUrl + "/storage/v1/object/" + bucket + "/" + key;
        try {
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(deleteUrl))
                .header("Authorization", "Bearer " + serviceKey)
                .DELETE()
                .build();
            HttpResponse<Void> resp = HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.discarding());
            if (resp.statusCode() >= 300) {
                throw new RuntimeException("Delete failed with status " + resp.statusCode() + " for URL: " + deleteUrl);
            }
            System.out.println("Successfully deleted object from Supabase: " + deleteUrl);
        } catch (Exception e) {
            System.err.println("Supabase delete failed for URL: " + deleteUrl);
            throw new RuntimeException("Supabase delete failed for bucket=" + bucket + ", key=" + key, e);
        }
    }
}