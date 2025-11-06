package nmc.assignments.msassignment.service;

import java.io.FileNotFoundException;
import java.nio.file.Path;

public interface PathSanitationService {
    Path sanitiseFile(String absolutePath, String relativePath) throws FileNotFoundException;

    String sanitisePath(String absolutePath);

    String sanitiseRelativePath(String relativePath);
}
