package nmc.assignments.msassignment.service.impl;

import nmc.assignments.msassignment.service.PathSanitationService;
import nmc.assignments.msassignment.service.StorageLocationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class StorageLocationServiceImpl implements StorageLocationService {
    private static final Logger logger = LogManager.getLogger(StorageLocationServiceImpl.class);

    @Value("${storage.location}")
    private String storageLocation;

    private String storageLocationAbsolutePath;

    @Autowired
    private PathSanitationService pathSanitationService;

    @Override
    public String getAbsolutePath(final String relativePath) {
        final String absolutePath = pathSanitationService.sanitisePath(getStorageLocationString() + "/" + relativePath);
        if (!absolutePath.startsWith(storageLocationAbsolutePath)) {
            final IllegalArgumentException exc = new IllegalArgumentException("The relative path is above the root storage directory. Path traversals outside of this domain are not allowed.");
            logger.throwing(exc);
        }
        return absolutePath;
    }

    @Override
    public String getRelativePath(final String absolutePath) {
        final String sanitisedAbsolutePath = pathSanitationService.sanitisePath(absolutePath);
        return sanitisedAbsolutePath.replace(getStorageLocationString() + "/", "");
    }

    @Override
    public Path getStorageLocationPath() {
        final Path path = Paths.get(getStorageLocationString());
        if (!Files.exists(path)) {
            logger.debug("Root storage location does not exist. Creating a new one.");
            try {
                Files.createDirectories(path);
            } catch (final IOException e) {
                final RuntimeException exc = new RuntimeException("Unable to create the root storage location.", e);
                throw logger.throwing(exc);
            }
        }
        return path;
    }

    @Override
    public String getStorageLocationString() {
        if (storageLocationAbsolutePath == null) {
            storageLocationAbsolutePath = pathSanitationService.sanitiseRelativePath(storageLocation);
        }
        return storageLocationAbsolutePath;
    }
}
