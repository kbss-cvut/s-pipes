package cz.cvut.spipes.debug.rest.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import cz.cvut.spipes.debug.service.ConfigurationService;

@RestController
public class SPipesDebugConfigurationController {

    private final ConfigurationService configurationService;

    public SPipesDebugConfigurationController(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    @Operation(summary = "Change rdf4j repository for SPipes debug")
    @PutMapping("/repository/{repositoryName}")
    public void changeRepository(@PathVariable String repositoryName) {
        configurationService.changeRepository(repositoryName);
    }
}
