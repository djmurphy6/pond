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

@Service
public class ImageService {

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

    public record ImageResult(byte[] bytes, String contentType) {}
}