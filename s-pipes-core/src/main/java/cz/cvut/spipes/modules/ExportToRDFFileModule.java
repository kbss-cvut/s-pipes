package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.SML;
import cz.cvut.spipes.engine.ExecutionContext;

/**
 * Created by Miroslav Blasko on 28.5.16.
 */
public class ExportToRDFFileModule extends AbstractModule {

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

    @Override
    public void loadConfiguration() {

    }

}
