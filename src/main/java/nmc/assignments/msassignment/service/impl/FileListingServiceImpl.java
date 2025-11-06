package nmc.assignments.msassignment.service.impl;

import nmc.assignments.msassignment.service.FileListingService;
import nmc.assignments.msassignment.service.StorageLocationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class FileListingServiceImpl implements FileListingService {
    private static final Logger logger = LogManager.getLogger(FileListingServiceImpl.class);

    @Autowired
    private StorageLocationService storageLocationService;

    @Override
    public List<String> listAllFiles() throws IOException {
        final Path storageDirectory = storageLocationService.getStorageLocationPath();

        try {
            return listFilesInDirectory(storageDirectory, "/");

        } catch (final IllegalStateException e) {
            logger.catching(e);
            final IOException exc = new IOException("An exception occurred when listing files.", e);
            throw logger.throwing(exc);
        }
    }

    private List<String> listFilesInDirectory(final Path directory, final String prefix) {
        final List<String> filenames = new ArrayList<>();

        try (var directoryStream = Files.list(directory)) {
            directoryStream.map(path -> {
                final List<String> filenameList;

                if (Files.isDirectory(path)) {
                    final String name = path.getFileName().toString();
                    filenameList = listFilesInDirectory(path, prefix + name + "/");

                } else if (Files.isRegularFile(path)) {
                    filenameList = Collections.singletonList(prefix + path.getFileName().toString());

                } else {
                    filenameList = Collections.emptyList();
                }
                return filenameList;
            }).forEach(filenames::addAll);

        } catch (final IOException e) {
            logger.catching(e);
            throw new IllegalStateException(e);
        }

        return filenames;
    }
}
