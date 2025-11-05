package nmc.assignments.msassignment.config;

import nmc.assignments.msassignment.service.FileDeletionService;
import nmc.assignments.msassignment.service.FileDownloadService;
import nmc.assignments.msassignment.service.FileListingService;
import nmc.assignments.msassignment.service.FileUploadService;
import nmc.assignments.msassignment.service.impl.FileDeletionServiceImpl;
import nmc.assignments.msassignment.service.impl.FileDownloadServiceImpl;
import nmc.assignments.msassignment.service.impl.FileListingServiceImpl;
import nmc.assignments.msassignment.service.impl.FileUploadServiceImpl;
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
    public FileDownloadService fileDownloadService() {
        return new FileDownloadServiceImpl();
    }

    @Bean
    public FileListingService fileListingService() {
        return new FileListingServiceImpl();
    }

    @Bean
    public FileUploadService fileUploadService() {
        return new FileUploadServiceImpl();
    }
}
