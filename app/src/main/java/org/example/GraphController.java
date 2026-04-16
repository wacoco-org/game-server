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

    private final GraphicsLoader loadGraphics;

    public GraphController(GraphicsLoader loadGraphics) {
        this.loadGraphics = loadGraphics;
    }

    @GetMapping(value = "/graph/{name}/{index}", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] getGraphic(@PathVariable String name, @PathVariable int index) throws IOException {
        java.util.List<BufferedImage> rotations = loadGraphics.getRotatedImages().get(name);
        if (rotations == null && !name.endsWith(".psd")) {
            rotations = loadGraphics.getRotatedImages().get(name + ".psd");
        }

        if (rotations == null) {
            throw new ResourceNotFoundException("Image not found: " + name);
        }

        if (index < 0 || index >= rotations.size()) {
            throw new ResourceNotFoundException("Rotation index out of bounds: " + index);
        }

        BufferedImage img = rotations.get(index);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        return baos.toByteArray();
    }
}

class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
