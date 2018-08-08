package cz.cvut.spipes.modules.eccairs;

import cz.cvut.kbss.eccairs.cfg.Configuration;
import cz.cvut.kbss.eccairs.schema.dao.SingeltonEccairsAccessFactory;
import cz.cvut.kbss.eccairs.schema.dao.cfg.EccairsAccessConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Properties;

public class EccairsAccessFactory {

    //TODO need synchronization within the factory writable methods !!!
    private static SingeltonEccairsAccessFactory eaf;


    public static @NotNull SingeltonEccairsAccessFactory getInstance()  {
        // create eccairs schema factory
        if (eaf == null) {
            try {
                // I - incialization of necessary objects. This block can be part of the configuration of the application
                // - create eccairs schema factory
                eaf = new SingeltonEccairsAccessFactory();
                //   - load cofig from properties file
                java.io.InputStream is = Configuration.class.getResourceAsStream("/eccairs-tools-config.properties");
                //LOG.debug(is.toString());
                Properties p = new Properties();
                p.load(is);
                eaf.setConfiguration(new EccairsAccessConfiguration(p));

            } catch (IOException ex) {
                throw new RuntimeException("Could not initialize eccairs access factory.", ex);
            }
        }
        return eaf;
    }
}
