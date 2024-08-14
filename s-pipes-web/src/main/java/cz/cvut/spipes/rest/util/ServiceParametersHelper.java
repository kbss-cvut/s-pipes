package cz.cvut.spipes.rest.util;

import cz.cvut.spipes.exception.SPipesServiceException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MultiValueMap;

@Slf4j
public class ServiceParametersHelper {

    @NotNull
    final MultiValueMap<String, String> parameters;


    public ServiceParametersHelper(@NotNull final MultiValueMap<String, String> parameters) {
        this.parameters = parameters;
    }


    @NotNull
    public String getRequiredParameterValue(@NotNull final String parameterKey) {
        if (!parameters.containsKey(parameterKey)) {
            throw new SPipesServiceException("Required parameter '" + parameterKey + "' was not supplied.");
        }
        return getParameterValue(parameterKey);
    }

    @NotNull
    public String getParameterValue(@NotNull final String parameterKey) {
        List<String> values = parameters.get(parameterKey);
        String lastValue = values.get(values.size() - 1);
        if (parameters.get(parameterKey).size() > 1) {
            log.warn("Parameter {} has multiple values: {}. Last assignment of the parameter, i.e. {}, will be used.",
                parameterKey,
                values,
                lastValue);
        }
        return lastValue;
    }

    @NotNull
    public URL parseParameterValueAsUrl(@NotNull final String parameterKey) {
        String value = getParameterValue(parameterKey);
        try {
            return new URL(parameters.getFirst(parameterKey));
        } catch (MalformedURLException e) {
            throw new SPipesServiceException("Invalid URL provided by parameter " + parameterKey + ", ", e);
        }
    }

    public boolean hasParameterValue(@NotNull final String parameterKey) {
        return parameters.containsKey(parameterKey);
    }

    public @NotNull
    Path parseParameterValueAsFilePath(@NotNull final String parameterKey) {
        try {
            final URL outputBindingURL = new URL(getParameterValue(parameterKey));
            if (!outputBindingURL.getProtocol().equals("file")) {
                throw new SPipesServiceException("Invalid URL provided by parameter " + parameterKey + " --  only file:// URI scheme is supported.");
            }
            return Paths.get(outputBindingURL.toURI());
        } catch (MalformedURLException | URISyntaxException e) {
            throw new SPipesServiceException("Invalid URL provided by parameter " + parameterKey + ", ", e);
        }
    }

}
