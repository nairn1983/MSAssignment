package nmc.assignments.msassignment.config;

import nmc.assignments.msassignment.service.StorageLocationService;
import nmc.assignments.msassignment.service.impl.StorageLocationServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageLocationConfig {
    @Bean
    public StorageLocationService storageLocationService() {
        return new StorageLocationServiceImpl();
    }
}
