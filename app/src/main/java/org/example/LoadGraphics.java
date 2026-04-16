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
public class LoadGraphics {

    private static final Logger logger = LoggerFactory.getLogger(LoadGraphics.class);

    @Value("classpath*:/art/*.psd")
    private Resource[] resources;

    private final Map<String, BufferedImage> images = new HashMap<>();

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

        BufferedImage rotated = new BufferedImage(newW, newH, image.getType());
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
        BufferedImage copy = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        Graphics2D g2d = copy.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        return copy;
    }

    public Map<String, BufferedImage> getImages() {
        return images;
    }
}