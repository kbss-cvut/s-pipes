package cz.cvut.spipes.migration.cli;

import cz.cvut.spipes.manager.OntoDocManager;
import cz.cvut.spipes.manager.OntologyDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.util.LocationMapper;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CliFileResolver {

    static final String S_PIPES_LIB_URI = "http://onto.fel.cvut.cz/ontologies/s-pipes-lib";

    static List<File> resolveFiles(List<File> paths, boolean onlyScriptFiles) {
        OntologyDocumentManager manager = OntoDocManager.getInstance();
        manager.registerDocuments(paths.stream().map(File::toPath).map(Path::toAbsolutePath).toList());

        LocationMapper lm = manager.getOntDocumentManager().getFileManager().getLocationMapper();

        List<File> result = new ArrayList<>();
        for (String uri : manager.getRegisteredOntologyUris()) {
            String filePath = lm.getAltEntry(uri);
            if (filePath == null) {
                continue;
            }

            File file = new File(filePath);
            if (!file.isFile()) {
                continue;
            }

            if (onlyScriptFiles) {
                OntModel model = manager.getOntology(uri);
                model.loadImports();
                if (!model.listImportedOntologyURIs(true).contains(S_PIPES_LIB_URI)) {
                    System.err.println("Skipped (not a script): " + filePath);
                    continue;
                }
            }

            result.add(file);
        }

        return result;
    }
}
