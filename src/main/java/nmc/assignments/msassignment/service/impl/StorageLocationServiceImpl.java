package nmc.assignments.msassignment.service.impl;

import nmc.assignments.msassignment.service.PathSanitationService;
import nmc.assignments.msassignment.service.StorageLocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class StorageLocationServiceImpl implements StorageLocationService {
    @Value("${storage.location}")
    private String storageLocation;

    private String storageLocationAbsolutePath;

    @Autowired
    private PathSanitationService pathSanitationService;

    @Override
    public String getAbsolutePath(final String relativePath) {
        return pathSanitationService.sanitisePath(getStorageLocationString() + "/" + relativePath);
    }

    @Override
    public Path getStorageLocationPath() {
        return Paths.get(getStorageLocationString());
    }

    @Override
    public String getStorageLocationString() {
        if (storageLocationAbsolutePath == null) {
            storageLocationAbsolutePath = pathSanitationService.sanitiseRelativePath(storageLocation);
        }
        return storageLocationAbsolutePath;
    }
}
