package nmc.assignments.msassignment.service.impl;

import nmc.assignments.msassignment.entity.FileInformation;
import nmc.assignments.msassignment.service.FileUploadService;
import nmc.assignments.msassignment.service.StorageLocationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileUploadServiceImpl implements FileUploadService {
    private static final Logger logger = LogManager.getLogger(FileUploadServiceImpl.class);

    @Autowired
    private StorageLocationService storageLocationService;

    @Override
    public FileInformation uploadFile(final String relativePath, final MultipartFile file) throws IOException {
        final String originalFilename = file.getOriginalFilename();
        String fullRelativePath = relativePath.endsWith("/")
            ? relativePath + originalFilename // relativePath is a directory - we will store using the same filename
            : relativePath;

        if (fullRelativePath.startsWith("/")) {
            // Strip leading forward slashes
            fullRelativePath = fullRelativePath.substring(1);
        }
        logger.info("Uploading file {} to {}", originalFilename, relativePath);

        final String absolutePath = storageLocationService.getAbsolutePath(fullRelativePath);
        final Path path = Paths.get(absolutePath);

        // Create subdirectories if they do not already exist
        Files.createDirectories(path.getParent());

        // Do not allow a file to be overwritten in this iteration
        if (Files.exists(path)) {
            final FileAlreadyExistsException exc = new FileAlreadyExistsException(fullRelativePath);
            logger.error("A file at path {} already exists. It is not possible to overwrite existing files. " +
                "If the file needs to be saved at this location, delete the existing file and upload the new one.", fullRelativePath);
            throw logger.throwing(exc);
        }

        try (final InputStream inputStream = file.getInputStream()) {
            logger.debug("Copying file {} to {}", originalFilename, fullRelativePath);
            Files.copy(file.getInputStream(), path);
        }

        final FileInformation fileInformation = createFileInformation(path);
        logger.debug("Copied file {}. Returning {}.", fullRelativePath, fileInformation);

        return fileInformation;
    }

    private FileInformation createFileInformation(final Path path) throws IOException {
        final String relativePath = storageLocationService.getRelativePath(path.toString());
        return new FileInformation(
            Files.probeContentType(path),
            path.getFileName().toString(),
            relativePath,
            Files.size(path));
    }
}
