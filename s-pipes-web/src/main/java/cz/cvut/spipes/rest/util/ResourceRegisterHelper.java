package cz.cvut.spipes.rest.util;

import cz.cvut.spipes.registry.StreamResource;
import cz.cvut.spipes.registry.StreamResourceRegistry;
import cz.cvut.spipes.rest.StreamResourceDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Component
public class ResourceRegisterHelper {

    public StreamResourceDTO registerStreamResource(String contentType, InputStream body) {
        StreamResourceDTO res = new StreamResourceDTO(
            UUID.randomUUID().toString(),
            StreamResourceRegistry.getInstance().getPERSISTENT_CONTEXT_PREFIX(),
            getRegisteredResourceLocation()
        );

        log.info("Registering new stream resource with id {} and url {} ", res.getId(), res.getPersistentUri());

        final byte[] data;
        try {
            data = IOUtils.toByteArray(body);
            StreamResource streamResource = StreamResourceRegistry.getInstance()
                .registerResource(res.getId(), data, contentType);
            res.attachStreamResource(streamResource);
            log.info("Resource content size: {}", data.length);
        } catch (IOException e) {
            log.error("Unable to read payload: ", e);
        }

        return res;
    }

    public String getRegisteredResourceLocation() {
        String resourcesLocation = ServletUriComponentsBuilder
            .fromCurrentContextPath().path("/resources/")
            .buildAndExpand("").toUriString();
        StreamResourceRegistry.getInstance().registerResourcePrefix(resourcesLocation); //TODO not very effective
        return resourcesLocation;
    }
}
