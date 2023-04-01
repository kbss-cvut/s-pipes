package cz.cvut.spipes.debug.rest.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import cz.cvut.spipes.debug.service.ConfigurationService;

@RestController
public class SPipesDebugConfigurationController {

    private final ConfigurationService configurationService;

    public SPipesDebugConfigurationController(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    @GetMapping("/repository/{repositoryName}")
    public void changeRepository(@PathVariable String repositoryName){
        configurationService.changeRepository(repositoryName);
    }
}
