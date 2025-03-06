package cz.cvut.spipes.debug.config;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class PropertyResolver {

    private final Environment environment;

    @Autowired
    public PropertyResolver(Environment environment) {
        this.environment = environment;
    }

    public String getProperty(String propertyName){
        String envVariableValue = System.getenv(replaceCamelCase(propertyName));
        return Objects.requireNonNullElseGet(envVariableValue, () -> environment.getRequiredProperty(propertyName));
    }

    private static String replaceCamelCase(String input) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (Character.isUpperCase(c)) {
                sb.append("_");
            }
            sb.append(Character.toUpperCase(c));
        }
        return sb.toString();
    }
}
