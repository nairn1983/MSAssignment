package nmc.assignments.msassignment.service;

import java.nio.file.Path;

public interface FileDownloadService {
    Path resolveFilename(String filename);
}
