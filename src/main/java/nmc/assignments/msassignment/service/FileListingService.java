package nmc.assignments.msassignment.service;

import java.io.IOException;
import java.util.List;

public interface FileListingService {
    List<String> listAllFiles() throws IOException;
}
