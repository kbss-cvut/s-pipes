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

    static final String S_PIPES_URI = "http://onto.fel.cvut.cz/ontologies/s-pipes";
    static final String S_PIPES_LIB_URI = "http://onto.fel.cvut.cz/ontologies/s-pipes-lib";

    record ResolveResult(List<File> filesToProcess, List<File> skippedNonScriptFiles) {}

    static ResolveResult resolveFiles(List<File> paths, boolean onlyScriptFiles) {
        OntologyDocumentManager manager = OntoDocManager.getInstance();
        manager.registerDocuments(paths.stream().map(File::toPath).map(Path::toAbsolutePath).toList());

        LocationMapper lm = manager.getOntDocumentManager().getFileManager().getLocationMapper();

        List<File> filesToProcess = new ArrayList<>();
        List<File> skippedNonScriptFiles = new ArrayList<>();

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
                var imports = model.listImportedOntologyURIs(true);
                if (!imports.contains(S_PIPES_URI) && !imports.contains(S_PIPES_LIB_URI)) {
                    skippedNonScriptFiles.add(file);
                    continue;
                }
            }

            filesToProcess.add(file);
        }

        return new ResolveResult(filesToProcess, skippedNonScriptFiles);
    }

    static void printSummary(List<File> skippedNonScriptFiles, List<File> skippedAlreadyProcessed,
                             List<File> processedFiles, String action) {
        printSummary(skippedNonScriptFiles, skippedAlreadyProcessed, List.of(), List.of(), processedFiles, action);
    }

    static void printSummary(List<File> skippedNonScriptFiles, List<File> skippedAlreadyProcessed,
                             List<File> skippedNotPreformatted, List<File> failedFiles,
                             List<File> processedFiles, String action) {
        System.out.println();
        if (!failedFiles.isEmpty()) {
            System.out.println(failedFiles.size() + " files failed to be " + action + " due to error (see above):");
            for (File f : failedFiles) {
                System.out.println("  - " + f.getAbsolutePath());
            }
        }
        if (!skippedNonScriptFiles.isEmpty()) {
            System.out.println(skippedNonScriptFiles.size() + " files skipped as non-script files because they do not import s-pipes or s-pipes-lib:");
            for (File f : skippedNonScriptFiles) {
                System.out.println("  - " + f.getAbsolutePath());
            }
        }
        if (!skippedAlreadyProcessed.isEmpty()) {
            System.out.println(skippedAlreadyProcessed.size() + " files skipped as already " + action + ":");
            for (File f : skippedAlreadyProcessed) {
                System.out.println("  - " + f.getAbsolutePath());
            }
        }
        if (!skippedNotPreformatted.isEmpty()) {
            System.out.println(skippedNotPreformatted.size() + " files skipped as not preformatted:");
            for (File f : skippedNotPreformatted) {
                System.out.println("  - " + f.getAbsolutePath());
            }
        }
        if (!processedFiles.isEmpty()) {
            System.out.println(processedFiles.size() + " files " + action + ":");
            for (File f : processedFiles) {
                System.out.println("  - " + f.getAbsolutePath());
            }
        }
        if (!skippedNotPreformatted.isEmpty()) {
            System.out.println();
            System.out.println("WARNING: Some files were not migrated because they are not preformatted. " +
                "Use 'reformat' command to preformat them first.");
        }
    }
}
