package nmc.assignments.msassignment.config;

import nmc.assignments.msassignment.service.FileDeletionService;
import nmc.assignments.msassignment.service.FileListingService;
import nmc.assignments.msassignment.service.impl.FileDeletionServiceImpl;
import nmc.assignments.msassignment.service.impl.FileListingServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FileServicesConfig {
    public static final String ENDPOINTS_ROOT = "/file";

    @Bean
    public FileDeletionService fileDeletionService() {
        return new FileDeletionServiceImpl();
    }

    @Bean
    public FileListingService fileListingService() {
        return new FileListingServiceImpl();
    }
}
