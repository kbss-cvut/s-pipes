package cz.cvut.spipes.rest.util;

import cz.cvut.spipes.registry.StreamResourceRegistry;
import cz.cvut.spipes.rest.StreamResourceDTO;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Component
public class ResourceRegisterHelper {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceRegisterHelper.class);

    public StreamResourceDTO registerStreamResource(String contentType, InputStream body) {
        StreamResourceDTO res = new StreamResourceDTO(
            UUID.randomUUID().toString(),
            StreamResourceRegistry.getInstance().getPERSISTENT_CONTEXT_PREFIX(),
            getRegisteredResourceLocation()
        );

        LOG.info("Registering new stream resource with id {} and url {} ", res.getId(), res.getPersistentUri());

        final byte[] data;
        try {
            data = IOUtils.toByteArray(body);
            StreamResourceRegistry.getInstance().registerResource(res.getId(), data, contentType);
            LOG.info("Resource content size: {}", data.length);
        } catch (IOException e) {
            LOG.error("Unable to read payload: ", e);
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
