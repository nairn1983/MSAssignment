package nmc.assignments.msassignment.service.impl;

import nmc.assignments.msassignment.service.ResourceService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.nio.file.Path;

@Service
public class ResourceServiceImpl implements ResourceService {
    @Override
    public Resource getUrlResourceFromPath(final Path path, final String relativePath)
        throws FileNotFoundException, MalformedURLException {

        final Resource resource = new UrlResource(path.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            throw new FileNotFoundException(relativePath);
        }

        return resource;
    }
}
