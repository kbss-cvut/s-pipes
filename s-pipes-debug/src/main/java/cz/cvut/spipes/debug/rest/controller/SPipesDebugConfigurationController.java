package cz.cvut.spipes.debug.rest.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import cz.cvut.spipes.debug.service.ConfigurationService;
import io.swagger.annotations.ApiOperation;

@RestController
public class SPipesDebugConfigurationController {

    private final ConfigurationService configurationService;

    public SPipesDebugConfigurationController(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    @ApiOperation(value = "Change rdf4j repository for SPipes debug")
    @PutMapping("/repository/{repositoryName}")
    public void changeRepository(@PathVariable String repositoryName) {
        configurationService.changeRepository(repositoryName);
    }
}
