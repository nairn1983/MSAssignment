package nmc.assignments.msassignment.service.impl;

import nmc.assignments.msassignment.service.FileListingService;
import nmc.assignments.msassignment.service.StorageLocationService;
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
    @Autowired
    private StorageLocationService storageLocationService;

    @Override
    public List<String> listAllFiles() {
        final Path storageDirectory = storageLocationService.getStorageLocationPath();
        return listFilesInDirectory(storageDirectory, "/");
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
            throw new RuntimeException(e);
        }

        return filenames;
    }
}
