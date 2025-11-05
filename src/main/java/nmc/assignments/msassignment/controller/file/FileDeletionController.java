package nmc.assignments.msassignment.controller.file;

import nmc.assignments.msassignment.service.FileDeletionService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.WebServerException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

import static nmc.assignments.msassignment.config.FileServicesConfig.ENDPOINTS_ROOT;

@RestController
public class FileDeletionController {
    private static final Logger logger = LogManager.getLogger(FileDeletionController.class);

    @Autowired
    private FileDeletionService fileDeletionService;

    @DeleteMapping(ENDPOINTS_ROOT + "/delete/{*filepath}")
    public ResponseEntity<?> deleteFile(@PathVariable final String filepath) {
        try {
            fileDeletionService.deleteFile(filepath);
            return new ResponseEntity<>(null, HttpStatus.OK);

        } catch (final IOException e) {
            logger.catching(e);
            final WebServerException exc = new WebServerException("Received I/O exception when deleting file " + filepath, e);
            throw logger.throwing(exc);

        } catch (final IllegalArgumentException e) {
            logger.catching(e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
