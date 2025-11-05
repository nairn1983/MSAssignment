package nmc.assignments.msassignment.service.impl;

import nmc.assignments.msassignment.service.FileDeletionService;
import nmc.assignments.msassignment.service.PathSanitationService;
import nmc.assignments.msassignment.service.StorageLocationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class FileDeletionServiceImpl implements FileDeletionService {
    private static final Logger logger = LogManager.getLogger(FileDeletionServiceImpl.class);

    @Autowired
    private PathSanitationService pathSanitationService;

    @Autowired
    private StorageLocationService storageLocationService;

    @Override
    public void deleteFile(final String filename) throws IOException {
        final String relativePath = filename.startsWith("/") ? filename.substring(1) : filename;
        logger.info("Deleting file: {}", relativePath);

        final String fullPathFilename = storageLocationService.getAbsolutePath(relativePath);
        final Path fullPath = pathSanitationService.sanitiseFile(fullPathFilename, relativePath);

        Files.delete(fullPath);
        logger.info("Deleted file: {}", relativePath);
    }
}
