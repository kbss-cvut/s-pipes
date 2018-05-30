package cz.cvut.sempipes.util;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.tdb.store.GraphTDB;

class TDBModelHelper {

    public static String getLocation(Model tdbModel) {
        return ((GraphTDB) tdbModel.getGraph()).getDSG().getLocation().getDirectoryPath().replaceAll("/$", "");
    }
}
