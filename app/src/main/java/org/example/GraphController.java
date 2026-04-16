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
public class GraphController {

    private final LoadGraphics loadGraphics;

    public GraphController(LoadGraphics loadGraphics) {
        this.loadGraphics = loadGraphics;
    }

    @GetMapping(value = "/graphics/{name}", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] getGraphic(@PathVariable String name) throws IOException {
        Map<String, BufferedImage> images = loadGraphics.getImages();
        
        // Ensure the name has .psd extension if it wasn't provided, 
        // or just look for the key as it was stored in LoadGraphics.
        BufferedImage img = images.get(name);
        if (img == null && !name.endsWith(".psd")) {
            img = images.get(name + ".psd");
        }

        if (img == null) {
            throw new ResourceNotFoundException("Image not found: " + name);
        }

        BufferedImage resized = loadGraphics.resize(img, Constants.SPRITE_SIZE, Constants.SPRITE_SIZE);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(resized, "png", baos);
        return baos.toByteArray();
    }
}

class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
