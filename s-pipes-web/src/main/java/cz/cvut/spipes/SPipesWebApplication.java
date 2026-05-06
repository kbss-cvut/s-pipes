package cz.cvut.spipes;

import cz.cvut.spipes.security.SecurityConstants;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.request.RequestContextListener;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class })
public class SPipesWebApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(SPipesWebApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(SPipesWebApplication.class);
    }

    @Bean
    public ServletListenerRegistrationBean<RequestContextListener> requestContextListener() {
        return new ServletListenerRegistrationBean<>(new RequestContextListener());
    }

    @Bean
    public ServletContextInitializer sessionCookieNameInitializer() {
        return servletContext -> servletContext.getSessionCookieConfig()
                .setName(SecurityConstants.SESSION_COOKIE_NAME);
    }
}
