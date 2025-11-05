package nmc.assignments.msassignment.service;

import java.nio.file.Path;

public interface PathSanitationService {
    Path sanitiseFile(String absolutePath, String relativePath);

    String sanitisePath(String absolutePath);

    String sanitiseRelativePath(String relativePath);
}
