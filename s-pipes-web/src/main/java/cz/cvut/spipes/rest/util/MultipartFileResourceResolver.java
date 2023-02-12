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
import java.util.List;
import java.util.Optional;

/**
 * Resolver of multipart files references within query parameters.
 */
@Component
public class MultipartFileResourceResolver {

    private static final Logger LOG = LoggerFactory.getLogger(MultipartFileResourceResolver.class);

    private final ResourceRegisterHelper resourceRegisterHelper;

    @Autowired
    public MultipartFileResourceResolver(ResourceRegisterHelper resourceRegisterHelper) {
        this.resourceRegisterHelper = resourceRegisterHelper;
    }

    /**
     * Resolves multipart files that are referenced by query parameters.The references within parameter values
     * are prefixed by symbol "@" followed by name of the file. Referenced file are registered
     * as {@code cz.cvut.spipes.registry.StreamResource} which produces URI that is used to replace the original
     * parameter value.
     * <p>
     * Example: "myParam"="@input.csv"
     * <p>
     * Within the example, "input.csv" is name of file searched within the {@code files}. It such file is found,
     * it is registered as {@code cz.cvut.spipes.registry.StreamResource} whose generated URL is replaced
     * back to "myParam". Then the parameter is replaced by new value e.g.
     * "myParam"="http://onto.fel.cvut.cz/resources/dea95aca-590e-4e77-8a0c-458814cc82b5
     *
     * @param parameters         Http query parameters.
     * @param files              Multipart files referenced by the parameters.
     * @param newStreamResources List of new stream resources created by this method,
     *                           empty list should be provided here.
     *                           // TODO this is parameter should be removed as it is workaround
     *                           //for issue #XXX
     * @return New parameters where each found reference is replaced by stream resource url.
     */
    public MultiValueMap<String, String> resolveResources(
        MultiValueMap<String, String> parameters,
        MultipartFile[] files,
        List<StreamResourceDTO> newStreamResources) {
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
                        newStreamResources.add(res);
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
