package nmc.assignments.msassignment.service;

import org.springframework.core.io.Resource;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.nio.file.Path;

public interface ResourceService {
    Resource getUrlResourceFromPath(Path path, String relativePath)
        throws FileNotFoundException, MalformedURLException;
}
