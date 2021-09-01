package cz.cvut.spipes.rest.util;

import cz.cvut.spipes.rest.StreamResourceDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

@Component
public class MultipartFileResourceResolver {

    private static final Logger LOG = LoggerFactory.getLogger(MultipartFileResourceResolver.class);

    private final ResourceRegisterHelper resourceRegisterHelper;

    @Autowired
    public MultipartFileResourceResolver(ResourceRegisterHelper resourceRegisterHelper) {
        this.resourceRegisterHelper = resourceRegisterHelper;
    }

    public MultiValueMap<String, String> resolveResources(MultiValueMap<String, String> parameters, MultipartFile[] files) {
        MultiValueMap<String, String> newParameters = new LinkedMultiValueMap<>(parameters);

        parameters.entrySet().stream()
            .filter(e -> e.getValue().stream()
                .anyMatch(v -> v.contains("@")))
            .forEach(e -> {
                String paramFilename = e.getValue().stream()
                    .filter(v -> v.contains("@"))
                    .findFirst().get(); // must be at least one present due to previous logic

                if (e.getValue().size() > 1) {
                    LOG.warn("Multiple values for url parameter: {}, using only first value: {}", e.getKey(), paramFilename);
                }

                String filename = paramFilename.replaceFirst("@", "");

                Optional<MultipartFile> multipartFileOptional = Arrays.stream(files)
                    .filter(f -> filename.equals(f.getOriginalFilename()))
                    .findFirst();
                if (multipartFileOptional.isPresent()) {
                    MultipartFile multipartFile = multipartFileOptional.get();
                    try {
                        StreamResourceDTO res = resourceRegisterHelper.registerStreamResource(multipartFile.getContentType(), multipartFile.getInputStream());
                        newParameters.replace(e.getKey(), Collections.singletonList(res.getPersistentUri()));
                    } catch (IOException ex) {
                        LOG.error(ex.getMessage(), ex);
                    }
                } else {
                    LOG.error("Missing multipart file for url parameter: {} with value: {}", e.getKey(), paramFilename);
                }
            });

        return newParameters;
    }
}
