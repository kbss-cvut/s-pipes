package cz.cvut.sempipes.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({WebAppConfig.class}) //PersistenceConfig.class, ServiceConfig.class, JmxConfig.class})
public class AppConfig {
}
