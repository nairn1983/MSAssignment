package nmc.assignments.msassignment.service.impl;

import nmc.assignments.msassignment.service.PathSanitationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PathSanitationServiceImpl implements PathSanitationService {
    private static final Logger logger = LogManager.getLogger(PathSanitationServiceImpl.class);

    @Override
    public Path sanitiseFile(final String filename) {
        final Path fullPath = Paths.get(filename);

        if (Files.isDirectory(fullPath)) {
            final IllegalArgumentException exc = new IllegalArgumentException("The path " + filename + " is a directory.");
            throw logger.throwing(exc);
        }

        if (!Files.exists(fullPath)) {
            final IllegalArgumentException exc = new IllegalArgumentException("The file " + filename + " does not exist.");
            throw logger.throwing(exc);
        }

        if (!Files.isRegularFile(fullPath)) {
            final IllegalArgumentException exc = new IllegalArgumentException("The location " + filename + " is not a regular file.");
            throw logger.throwing(exc);
        }
        return fullPath;
    }

    @Override
    public String sanitisePath(final String absolutePath) {
        // Convert system-specific directory separators
        final String replacedPath = absolutePath.replace("\\", "/");

        final Path path = Paths.get(replacedPath);
        final Path normalisedPath = path.normalize();

        final String pathString = path.toString();

        // Verify that the path is unchanged after normalisation to prevent path traversal
        if (!pathString.equals(normalisedPath.toString())) {
            final IllegalStateException exc = new IllegalStateException("The normalised path does not match the original path.");
            throw logger.throwing(exc);
        }

        return pathString;
    }

    @Override
    public String sanitiseRelativePath(final String relativePath) {
        final String absolutePath = Paths.get(relativePath)
            .toAbsolutePath()
            .toString();

        return sanitisePath(absolutePath);
    }
}
