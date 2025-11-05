package nmc.assignments.msassignment.service;

import java.nio.file.Path;

public interface StorageLocationService {
    String getAbsolutePath(String relativePath);

    Path getStorageLocationPath();

    String getStorageLocationString();
}
