package nmc.assignments.msassignment.config;

import nmc.assignments.msassignment.service.PathSanitationService;
import nmc.assignments.msassignment.service.impl.PathSanitationServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PathSanitationConfig {
    @Bean
    public PathSanitationService pathSanitationService() {
        return new PathSanitationServiceImpl();
    }
}
