package nmc.assignments.msassignment.service.impl;

import nmc.assignments.msassignment.entity.DownloadedFileInformation;
import nmc.assignments.msassignment.service.FileDownloadService;
import nmc.assignments.msassignment.service.PathSanitationService;
import nmc.assignments.msassignment.service.StorageLocationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class FileDownloadServiceImpl implements FileDownloadService {
    private static final Logger logger = LogManager.getLogger(FileDownloadServiceImpl.class);

    @Autowired
    private PathSanitationService pathSanitationService;

    @Autowired
    private StorageLocationService storageLocationService;

    @Override
    public DownloadedFileInformation downloadFile(String relativePath) throws IOException {
        logger.info("Downloading file {}", relativePath);

        final Path path = resolveFilename(relativePath);
        final Resource resource = new UrlResource(path.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            throw new FileNotFoundException(relativePath);
        }

        String contentType = Files.probeContentType(path);
        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            logger.debug("File content type was not found. Defaulting to {}", contentType);
        }

        final String filename = path.getFileName().toString();
        final long fileSize = Files.size(path);

        return new DownloadedFileInformation(
            contentType,
            filename,
            resource,
            fileSize);
    }

    private Path resolveFilename(final String filename) {
        final String fullPathFilename = storageLocationService.getAbsolutePath(filename);
        return pathSanitationService.sanitiseFile(fullPathFilename, filename);
    }
}
