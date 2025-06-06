package cz.cvut.spipes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class })
public class SPipesWebApplication   {
    public static void main(String[] args) {
        SpringApplication.run(SPipesWebApplication.class, args);
    }
}