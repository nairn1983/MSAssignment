package nmc.assignments.msassignment.service;

public interface PathSanitationService {
    String sanitisePath(final String absolutePath);

    String sanitiseRelativePath(final String relativePath);
}
