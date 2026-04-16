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
                            // rotate now handles resizing/cropping to SPRITE_SIZE internally
                            BufferedImage rotated = rotate(img, degrees);
                            rotations.add(rotated);
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
        int spriteSize = Constants.SPRITE_SIZE;
        int largeSize = spriteSize * 4;
        
        // 1. Create a large canvas (4 * SPRITE_SIZE)
        BufferedImage largeCanvas = new BufferedImage(largeSize, largeSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = largeCanvas.createGraphics();
        
        // 2. Setup rotation
        double radians = Math.toRadians(degrees);
        AffineTransform at = new AffineTransform();
        
        // Move to center of large canvas
        at.translate(largeSize / 2.0, largeSize / 2.0);
        // Rotate
        at.rotate(radians);
        
        // Scale to SPRITE_SIZE
        double scaleX = (double) spriteSize / image.getWidth();
        double scaleY = (double) spriteSize / image.getHeight();
        at.scale(scaleX, scaleY);
        
        // Move back by half of original image size to center it
        at.translate(-image.getWidth() / 2.0, -image.getHeight() / 2.0);
        
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setTransform(at);
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();

        // 3. Crop the SPRITE_SIZE square in the middle
        int x = (largeSize - spriteSize) / 2;
        int y = (largeSize - spriteSize) / 2;
        
        return largeCanvas.getSubimage(x, y, spriteSize, spriteSize);
    }

    public BufferedImage copy(BufferedImage image) {
        BufferedImage copy = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = copy.createGraphics();
        g2d.setComposite(java.awt.AlphaComposite.Src);
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        return copy;
    }

    public BufferedImage resize(BufferedImage image, int targetWidth, int targetHeight) {
        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.setComposite(java.awt.AlphaComposite.Src);
        g2d.drawImage(image, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();
        return resized;
    }

    public BufferedImage generateSpriteSheet(BufferedImage original) {
        int spriteSize = Constants.SPRITE_SIZE;
        int numRotations = 32;
        BufferedImage sheet = new BufferedImage(spriteSize * numRotations, spriteSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = sheet.createGraphics();
        
        for (int i = 0; i < numRotations; i++) {
            double degrees = (360.0 / numRotations) * i;
            BufferedImage rotated = rotate(original, degrees);
            g2d.drawImage(rotated, i * spriteSize, 0, null);
        }

        g2d.dispose();
        return sheet;
    }

    public BufferedImage generateCombinedSpriteSheet() {
        int spriteSize = Constants.SPRITE_SIZE;
        int numRotations = 32;
        int numUnits = rotatedImages.size();
        
        if (numUnits == 0) {
            return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        }

        BufferedImage combinedSheet = new BufferedImage(spriteSize * numRotations, spriteSize * numUnits, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = combinedSheet.createGraphics();

        int row = 0;
        // Sort keys to have deterministic output
        java.util.List<String> sortedNames = new java.util.ArrayList<>(rotatedImages.keySet());
        java.util.Collections.sort(sortedNames);

        for (String name : sortedNames) {
            java.util.List<BufferedImage> rotations = rotatedImages.get(name);
            for (int i = 0; i < rotations.size(); i++) {
                g2d.drawImage(rotations.get(i), i * spriteSize, row * spriteSize, null);
            }
            row++;
        }

        g2d.dispose();
        return combinedSheet;
    }

    public Map<String, BufferedImage> getImages() {
        return images;
    }

    public Map<String, java.util.List<BufferedImage>> getRotatedImages() {
        return rotatedImages;
    }
}