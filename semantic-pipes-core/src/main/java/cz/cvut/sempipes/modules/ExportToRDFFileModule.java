package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.constants.SML;
import cz.cvut.sempipes.engine.ExecutionContext;

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
