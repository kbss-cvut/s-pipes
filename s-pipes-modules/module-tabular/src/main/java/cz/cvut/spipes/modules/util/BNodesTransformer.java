package cz.cvut.spipes.modules.util;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.util.ResourceUtils;

import java.util.*;

public class BNodesTransformer {

    private final Map<Resource, Resource> convertedNodes =  new HashMap<>();

    public Model convertBNodesToNonBNodes(Model model) {
        Set<Resource> resourcesToConvert =  new HashSet<>();

        model
            .listStatements()
            .filterKeep(statement -> statement.getSubject().isAnon())
            .mapWith(Statement::getSubject)
            .forEach(resourcesToConvert::add);

        resourcesToConvert
            .forEach(resource -> {
                Resource convertedResource = ResourceUtils.renameResource(resource, "https://example.com/resource/" + resource.getId());
                convertedNodes.put(resource, convertedResource);
            });
        return model;
    }

    public Model transferJOPAEntitiesToBNodes(Model model){
        convertedNodes
                .forEach((resource,convertedR) ->
                        ResourceUtils.renameResource(model.getResource(convertedR.getURI()), null));

        transferGeneratedJOPAEntitiesToBNodes(model);
        return model;
    }

    public void transferGeneratedJOPAEntitiesToBNodes(Model model){
        model.listStatements()
                .filterKeep(statement -> statement.getSubject().getURI() != null)
                .filterKeep(statement -> statement.getSubject().getURI().contains("instance"))
                .mapWith(Statement::getSubject)
                .toSet().forEach(resource -> ResourceUtils.renameResource(resource, null));
    }
}
