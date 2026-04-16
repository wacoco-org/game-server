package org.example;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

@Component
public class LoadGraphics {

    @Value("classpath*:/art/*.psd")
    private Resource[] resources;

    @EventListener(ApplicationReadyEvent.class)
    public void loadGraphics() {
        if (resources != null) {
            Stream.of(resources)
                    .map(Resource::getFilename)
                    .forEach(System.out::println);
        }
    }
}