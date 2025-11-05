package nmc.assignments.msassignment.service.impl;

import nmc.assignments.msassignment.service.FileDownloadService;
import nmc.assignments.msassignment.service.PathSanitationService;
import nmc.assignments.msassignment.service.StorageLocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
public class FileDownloadServiceImpl implements FileDownloadService {
    @Autowired
    private PathSanitationService pathSanitationService;

    @Autowired
    private StorageLocationService storageLocationService;

    @Override
    public Path resolveFilename(final String filename) {
        final String fullPathFilename = storageLocationService.getAbsolutePath(filename);
        return pathSanitationService.sanitiseFile(fullPathFilename, filename);
    }
}
