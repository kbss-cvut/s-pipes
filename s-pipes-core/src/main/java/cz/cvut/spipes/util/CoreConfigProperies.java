package cz.cvut.spipes.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class CoreConfigProperies {
    private static final String CONFIG_FILE = "config-core.properties";
    private static final java.util.Properties prop = new java.util.Properties();
    private static final Logger log = LoggerFactory.getLogger(CoreConfigProperies.class);
    private static final String variableAssignmentPrefix = "variable.assignment";

    static {
        try {
            InputStream is = CoreConfigProperies.class.getClassLoader().getResourceAsStream("config-core.properties");
            if (is != null) {
                prop.load(is);
                prop.keySet().forEach(k -> {
                    String ks = k.toString();
                    String envValue = getEnvValue(ks);
                    if (envValue != null) {
                        log.debug("Overriding configuration property '{}' by system environment variable." +
                                " Using new value: {}.",
                                ks,
                                envValue
                        );
                        prop.setProperty(ks, envValue);
                    }
                });
                log.info("Loaded configuration from {} and system environment : \n {}", CONFIG_FILE, prop.entrySet());
            } else {
                throw new FileNotFoundException("Property file '" + CONFIG_FILE + "' not found in the classpath");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void set(String name, String value) {
        prop.setProperty(name, value);
    }

    public static String get(String name) {
        String value = prop.getProperty(name);
        if (value == null) {
            log.error("Property with key {} does not exist in loaded configuration file {}.", name, CONFIG_FILE);
            throw new IllegalArgumentException("Unable to load property " + name + " from configuration files.");
        }
        return value;
    }

    public static String get(String name, String defaultValue) {
        String value = prop.getProperty(name);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    private static String getEnvValue(String name){
        return System.getenv(name.toUpperCase().replaceAll("\\.", "_"));
    }

    public static String getConfigurationVariable(String name) {
        return get(variableAssignmentPrefix + "." + name);
    }
}
