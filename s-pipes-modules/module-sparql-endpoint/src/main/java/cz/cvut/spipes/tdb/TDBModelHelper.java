package cz.cvut.spipes.tdb;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.tdb1.store.GraphTDB;

class TDBModelHelper {

    public static String getLocation(Model tdbModel) {
        return ((GraphTDB) tdbModel.getGraph()).getDatasetGraphTDB()
                .getLocation()
                .getDirectoryPath()
                .replaceAll("/$", "")
                .replaceAll("\\\\$", "");
    }
}
