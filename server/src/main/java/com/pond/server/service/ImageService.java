package com.pond.server.service;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service class for image processing operations.
 * Handles image resizing, compression, and conversion to JPEG format.
 */
@Service
public class ImageService {

    /**
     * Processes an uploaded image file by resizing and compressing it.
     * Maintains aspect ratio and converts to JPEG format.
     *
     * @param file the uploaded image file
     * @param maxW the maximum width in pixels
     * @param maxH the maximum height in pixels
     * @param quality the JPEG compression quality (0.0-1.0)
     * @return ImageResult containing the processed image bytes and content type
     * @throws RuntimeException if image processing fails or file is invalid
     */
    public ImageResult process(MultipartFile file, int maxW, int maxH, float quality)  {
        try (InputStream in = file.getInputStream()) {
            BufferedImage src = ImageIO.read(in);
            if (src == null) throw new RuntimeException("Invalid image file");

            double scale = Math.min((double) maxW / src.getWidth(), (double) maxH / src.getHeight());
            scale = Math.min(scale, 1.0);
            int w = (int) Math.round(src.getWidth() * scale);
            int h = (int) Math.round(src.getHeight() * scale);

            BufferedImage dst = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = dst.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.drawImage(src, 0, 0, w, h, null);
            g.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality);
            param.setProgressiveMode(ImageWriteParam.MODE_DEFAULT);
            writer.setOutput(ImageIO.createImageOutputStream(baos));
            writer.write(null, new IIOImage(dst, null, null), param);
            writer.dispose();

            return new ImageResult(baos.toByteArray(), "image/jpeg");
        } catch (IOException e) {
            throw new RuntimeException("Failed to process image", e);
        }
    }

    /**
     * Processes raw image bytes by resizing and compressing them.
     * Maintains aspect ratio and converts to JPEG format.
     *
     * @param data the raw image bytes
     * @param maxW the maximum width in pixels
     * @param maxH the maximum height in pixels
     * @param quality the JPEG compression quality (0.0-1.0)
     * @return ImageResult containing the processed image bytes and content type
     * @throws RuntimeException if image processing fails or data is invalid
     */
    public ImageResult process(byte[] data, int maxW, int maxH, float quality)  {
        try (InputStream in = new java.io.ByteArrayInputStream(data)) {
            java.awt.image.BufferedImage src = javax.imageio.ImageIO.read(in);
            if (src == null) throw new RuntimeException("Invalid image data");
    
            double scale = Math.min((double) maxW / src.getWidth(), (double) maxH / src.getHeight());
            scale = Math.min(scale, 1.0);
            int w = (int) Math.round(src.getWidth() * scale);
            int h = (int) Math.round(src.getHeight() * scale);
    
            java.awt.image.BufferedImage dst = new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g = dst.createGraphics();
            g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g.drawImage(src, 0, 0, w, h, null);
            g.dispose();
    
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            javax.imageio.ImageWriter writer = javax.imageio.ImageIO.getImageWritersByFormatName("jpg").next();
            javax.imageio.ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality);
            param.setProgressiveMode(javax.imageio.ImageWriteParam.MODE_DEFAULT);
            writer.setOutput(javax.imageio.ImageIO.createImageOutputStream(baos));
            writer.write(null, new javax.imageio.IIOImage(dst, null, null), param);
            writer.dispose();
    
            return new ImageResult(baos.toByteArray(), "image/jpeg");
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to process image", e);
        }
    }

    /**
     * Record representing the result of image processing.
     *
     * @param bytes the processed image bytes
     * @param contentType the MIME type of the processed image
     */
    public record ImageResult(byte[] bytes, String contentType) {}
}