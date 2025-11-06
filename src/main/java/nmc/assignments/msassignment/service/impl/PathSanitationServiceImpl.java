package nmc.assignments.msassignment.service.impl;

import nmc.assignments.msassignment.service.PathSanitationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PathSanitationServiceImpl implements PathSanitationService {
    private static final Logger logger = LogManager.getLogger(PathSanitationServiceImpl.class);

    @Override
    public Path sanitiseFile(final String absolutePath, final String relativePath) throws FileNotFoundException {
        final Path fullPath = Paths.get(absolutePath);

        if (Files.isDirectory(fullPath)) {
            final IllegalArgumentException exc = new IllegalArgumentException("The path " + relativePath + " is a directory.");
            throw logger.throwing(exc);
        }

        if (!Files.exists(fullPath)) {
            final FileNotFoundException exc = new FileNotFoundException("The file " + relativePath + " does not exist.");
            throw logger.throwing(exc);
        }

        if (!Files.isRegularFile(fullPath)) {
            final IllegalArgumentException exc = new IllegalArgumentException("The location " + relativePath + " is not a regular file.");
            throw logger.throwing(exc);
        }
        return fullPath;
    }

    @Override
    public String sanitisePath(final String absolutePath) {
        final Path path = Paths.get(absolutePath);
        final Path normalisedPath = path.normalize();

        final String pathString = path.toString();

        // Verify that the path is unchanged after normalisation to prevent path traversal
        if (!pathString.equals(normalisedPath.toString())) {
            final IllegalStateException exc = new IllegalStateException("The normalised path does not match the original path.");
            throw logger.throwing(exc);
        }

        // Convert system-specific directory separators before returning
        return pathString.replace("\\", "/");
    }

    @Override
    public String sanitiseRelativePath(final String relativePath) {
        final String absolutePath = Paths.get(relativePath)
            .toAbsolutePath()
            .toString();

        return sanitisePath(absolutePath);
    }
}
