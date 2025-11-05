package nmc.assignments.msassignment.service.impl;

import nmc.assignments.msassignment.service.FileDeletionService;
import nmc.assignments.msassignment.service.StorageLocationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileDeletionServiceImpl implements FileDeletionService {
    private static final Logger logger = LogManager.getLogger(FileDeletionServiceImpl.class);

    @Autowired
    private StorageLocationService storageLocationService;

    @Override
    public void deleteFile(final String filename) throws IOException {
        logger.info("Deleting file: {}", filename);

        final String fullPathFilename = storageLocationService.getAbsolutePath(filename);
        final Path fullPath = Paths.get(fullPathFilename);

        if (Files.isDirectory(fullPath)) {
            final IllegalArgumentException exc = new IllegalArgumentException("The path " + filename + " is a directory.");
            throw logger.throwing(exc);
        }

        if (!Files.deleteIfExists(fullPath)) {
            final IllegalArgumentException exc = new IllegalArgumentException("The file " + filename + " does not exist.");
            throw logger.throwing(exc);
        }

        logger.info("Deleted file: {}", filename);
    }
}
