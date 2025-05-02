package cz.cvut.spipes.debug;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class SPipesDebugApplication {
    public static void main(String[] args) {
        SpringApplication.run(SPipesDebugApplication.class, args);
    }
}
