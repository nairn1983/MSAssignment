package nmc.assignments.msassignment.config;

import nmc.assignments.msassignment.service.ResourceService;
import nmc.assignments.msassignment.service.impl.ResourceServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResourceServiceConfig {
    @Bean
    public ResourceService resourceService() {
        return new ResourceServiceImpl();
    }
}
