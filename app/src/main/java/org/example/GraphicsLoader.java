package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class GraphicsLoader {

    private static final Logger logger = LoggerFactory.getLogger(GraphicsLoader.class);

    @Value("classpath*:/art/*.psd")
    private Resource[] resources;

    private final Map<String, BufferedImage> images = new HashMap<>();
    private final Map<String, java.util.List<BufferedImage>> rotatedImages = new HashMap<>();

    @EventListener(ApplicationReadyEvent.class)
    public void loadGraphics() {
        if (resources != null) {
            for (Resource resource : resources) {
                try {
                    BufferedImage img = ImageIO.read(resource.getInputStream());
                    if (img != null) {
                        String name = resource.getFilename();
                        images.put(name, img);
                        logger.info("Loaded PSD: {} ({}x{})", name, img.getWidth(), img.getHeight());
                        
                        // Pre-generate 32 rotations (resized to SPRITE_SIZE)
                        java.util.List<BufferedImage> rotations = new java.util.ArrayList<>();
                        int numRotations = 32;
                        int spriteSize = Constants.SPRITE_SIZE;
                        for (int i = 0; i < numRotations; i++) {
                            double degrees = (360.0 / numRotations) * i;
                            BufferedImage rotated = rotate(img, degrees);
                            BufferedImage resized = resize(rotated, spriteSize, spriteSize);
                            rotations.add(resized);
                        }
                        rotatedImages.put(name, rotations);
                        logger.info("Pre-generated 32 rotations for: {}", name);
                        
                        // Demonstration: Rotate and Copy
                        if ("battleship.psd".equals(name)) {
                            BufferedImage rotated = rotate(img, 90);
                            logger.info("Rotated battleship.psd by 90 degrees. New dimensions: {}x{}", rotated.getWidth(), rotated.getHeight());
                            
                            BufferedImage copied = copy(img);
                            logger.info("Created a copy of battleship.psd. Hash code: original={}, copy={}", img.hashCode(), copied.hashCode());
                        }
                    } else {
                        logger.warn("Failed to load PSD (unsupported or corrupt): {}", resource.getFilename());
                    }
                } catch (IOException e) {
                    logger.error("Error reading PSD resource {}: {}", resource.getFilename(), e.getMessage());
                }
            }
            logger.info("Total images loaded into memory: {}", images.size());
        }
    }

    public BufferedImage rotate(BufferedImage image, double degrees) {
        double radians = Math.toRadians(degrees);
        double sin = Math.abs(Math.sin(radians));
        double cos = Math.abs(Math.cos(radians));
        int w = image.getWidth();
        int h = image.getHeight();
        int newW = (int) Math.floor(w * cos + h * sin);
        int newH = (int) Math.floor(h * cos + w * sin);
        
        // Use TYPE_INT_ARGB to support transparency
        BufferedImage rotated = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = rotated.createGraphics();
        AffineTransform at = new AffineTransform();
        at.translate((newW - w) / 2.0, (newH - h) / 2.0);
        at.rotate(radians, w / 2.0, h / 2.0);
        g2d.setTransform(at);
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();

        return rotated;
    }

    public BufferedImage copy(BufferedImage image) {
        BufferedImage copy = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = copy.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        return copy;
    }

    public BufferedImage resize(BufferedImage image, int targetWidth, int targetHeight) {
        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(image, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();
        return resized;
    }

    public BufferedImage generateSpriteSheet(BufferedImage original) {
        int spriteSize = Constants.SPRITE_SIZE;
        int numRotations = 32;
        BufferedImage sheet = new BufferedImage(spriteSize * numRotations, spriteSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = sheet.createGraphics();

        // Resize the original to SPRITE_SIZE first to ensure we rotate the correctly sized sprite
        // or rotate first then resize? Rotating 16x16 can lose detail.
        // Let's resize the original to 16x16 and then rotate, or rotate the original and resize each rotation.
        // Rotating then resizing is better for quality.
        
        for (int i = 0; i < numRotations; i++) {
            double degrees = (360.0 / numRotations) * i;
            BufferedImage rotated = rotate(original, degrees);
            BufferedImage resized = resize(rotated, spriteSize, spriteSize);
            g2d.drawImage(resized, i * spriteSize, 0, null);
        }

        g2d.dispose();
        return sheet;
    }

    public Map<String, BufferedImage> getImages() {
        return images;
    }

    public Map<String, java.util.List<BufferedImage>> getRotatedImages() {
        return rotatedImages;
    }
}