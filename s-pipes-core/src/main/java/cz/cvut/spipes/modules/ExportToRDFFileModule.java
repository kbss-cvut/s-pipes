package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.SML;
import cz.cvut.spipes.engine.ExecutionContext;

public class ExportToRDFFileModule extends AnnotatedAbstractModule {

    //sml:baseURI
    //sml:targetFilePath

    @Override
    public ExecutionContext executeSelf() {
        return null;
    }

    @Override
    public String getTypeURI() {
        return SML.ExportToRDFFile.getURI();
    }

}
