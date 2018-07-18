package cz.cvut.sempipes.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Miroslav Blasko on 10.6.16.
 */
public class ConfigProperies {
    private static final String CONFIG_FILE = "config.properties";
    private static final java.util.Properties prop = new java.util.Properties();
    private static final Logger LOG = LoggerFactory.getLogger(ConfigProperies.class);

    static {
        try {

            InputStream is = ConfigProperies.class.getClassLoader().getResourceAsStream("config.properties");
            if (is != null) {
                prop.load(is);
                LOG.info("Loaded configuration from {} : \n {}", CONFIG_FILE, prop.entrySet());
            } else {
                throw new FileNotFoundException("Property file '" + CONFIG_FILE + "' not found in the classpath");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String get(String name) {
        String value = prop.getProperty(name);
        if (value == null) {
            LOG.error("Property with key {} does not exist in loaded configuration file {}.", name, CONFIG_FILE);
            throw new IllegalArgumentException("Unable to load property " + name + " from configuration files.");
        }
        return value;
    }
}
