package nmc.assignments.msassignment.controller.file;

import nmc.assignments.msassignment.service.FileListingService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.WebServerException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

import static nmc.assignments.msassignment.config.FileServicesConfig.ENDPOINTS_ROOT;

@RestController
public class FileListingController {
    private static final Logger logger = LogManager.getLogger(FileListingController.class);

    @Autowired
    private FileListingService fileListingService;

    @GetMapping(ENDPOINTS_ROOT + "/list")
    public ResponseEntity<List<String>> listAllFiles() {
        try {
            return new ResponseEntity<>(fileListingService.listAllFiles(), HttpStatus.OK);

        } catch (final IOException e) {
            logger.catching(e);
            final WebServerException exc = new WebServerException("Failed to list contents of storage directory. An I/O error occurred: " + e.getMessage(), e);
            throw logger.throwing(exc);
        }
    }
}
