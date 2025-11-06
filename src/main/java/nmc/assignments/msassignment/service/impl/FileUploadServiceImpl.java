package nmc.assignments.msassignment.service.impl;

import nmc.assignments.msassignment.entity.UploadedFileInformation;
import nmc.assignments.msassignment.service.FileUploadService;
import nmc.assignments.msassignment.service.StorageLocationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static nmc.assignments.msassignment.config.FileServicesConfig.ENDPOINTS_ROOT;

@Service
public class FileUploadServiceImpl implements FileUploadService {
    private static final Logger logger = LogManager.getLogger(FileUploadServiceImpl.class);

    @Autowired
    private StorageLocationService storageLocationService;

    @Override
    public UploadedFileInformation uploadFile(final String relativePath, final MultipartFile file) throws IOException {
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
            Files.copy(inputStream, path);
        }

        final UploadedFileInformation uploadedFileInformation = createFileInformation(path);
        logger.debug("Copied file {}. Returning {}.", fullRelativePath, uploadedFileInformation);

        return uploadedFileInformation;
    }

    @Override
    public URI createUriFromUploadedFile(
        final UploadedFileInformation uploadedFileInformation,
        final String relativePath) {

        return ServletUriComponentsBuilder.fromCurrentContextPath()
            .path(ENDPOINTS_ROOT + "/download/")
            .path(relativePath)
            .build()
            .toUri();
    }

    private UploadedFileInformation createFileInformation(final Path path) throws IOException {
        final String relativePath = storageLocationService.getRelativePath(path.toString());
        return new UploadedFileInformation(
            Files.probeContentType(path),
            path.getFileName().toString(),
            relativePath,
            Files.size(path));
    }
}
