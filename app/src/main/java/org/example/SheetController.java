package org.example;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

@RestController
public class SheetController {

    private final GraphicsLoader loadGraphics;

    public SheetController(GraphicsLoader loadGraphics) {
        this.loadGraphics = loadGraphics;
    }

    @GetMapping(value = "/sheets/{name}", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] getSheet(@PathVariable String name) throws IOException {
        Map<String, BufferedImage> images = loadGraphics.getImages();
        
        BufferedImage img = images.get(name);
        if (img == null && !name.endsWith(".psd")) {
            img = images.get(name + ".psd");
        }

        if (img == null) {
            throw new ResourceNotFoundException("Image not found: " + name);
        }

        BufferedImage sheet = loadGraphics.generateSpriteSheet(img);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(sheet, "png", baos);
        return baos.toByteArray();
    }
}
