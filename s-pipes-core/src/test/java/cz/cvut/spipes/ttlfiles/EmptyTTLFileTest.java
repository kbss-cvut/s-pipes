package cz.cvut.spipes.ttlfiles;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

public class EmptyTTLFileTest {

    @Test
    @DisplayName("TTL file shouldn't be empty")
    public void TTLFileNotEmpty(){
        Model model = ModelFactory.createDefaultModel();
        String path = "../doc/examples/hello-world/hello-world.sms.ttl";
//        String path = "../doc/examples/hello-world/empty.ttl";
        InputStream in = FileManager.get().open(path);
        model.read(in,null,"TURTLE");

        Assertions.assertFalse(model.isEmpty());
        if(!model.isEmpty()){
//            model.write(System.out);
//            System.out.println("------------TURTLE SYNTAX BELOW------------");
            model.write(System.out,"TURTLE");
        }
    }
}
