package cz.cvut.spipes.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class CoreConfigProperies {
    public static final String variableAssignmentPrefix = "variable.assignment";
    private static final String CONFIG_FILE = "config-core.properties";
    private static final java.util.Properties prop = new java.util.Properties();
    private static final Logger LOG = LoggerFactory.getLogger(CoreConfigProperies.class);

    static {
        try {
            InputStream is = CoreConfigProperies.class.getClassLoader().getResourceAsStream("config-core.properties");
            if (is != null) {
                prop.load(is);
                prop.keySet().forEach(k -> {
                    String ks = k.toString();
                    String envValue = getEnvValue(ks);
                    if (envValue != null) {
                        LOG.debug("Overriding configuration property '{}' by system environment variable." +
                                " Using new value: {}.",
                                ks,
                                envValue
                        );
                        prop.setProperty(ks, envValue);
                    }
                });
                LOG.info("Loaded configuration from {} and system environment : \n {}", CONFIG_FILE, prop.entrySet());
            } else {
                throw new FileNotFoundException("Property file '" + CONFIG_FILE + "' not found in the classpath");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<String,String> getConfigurationVariables(){
        Map<String, String> map = new HashMap<>((Map) prop);
        return map.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> y, HashMap::new));
    }

    public static String get(String name) {
        String value = prop.getProperty(name);
        if (value == null) {
            LOG.error("Property with key {} does not exist in loaded configuration file {}.", name, CONFIG_FILE);
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

}
