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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.NoSuchFileException;

import static nmc.assignments.msassignment.config.FileServicesConfig.ENDPOINTS_ROOT;

@RestController
public class FileDeletionController {
    private static final Logger logger = LogManager.getLogger(FileDeletionController.class);

    @Autowired
    private FileDeletionService fileDeletionService;

    @DeleteMapping(ENDPOINTS_ROOT + "/delete/{*filepath}")
    public ResponseEntity<?> deleteFile(@PathVariable final String filepath) {
        final String relativePath = filepath.startsWith("/") ? filepath.substring(1) : filepath;
        try {
            fileDeletionService.deleteFile(relativePath);
            return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);

        } catch (final AccessDeniedException | FileNotFoundException | NoSuchFileException e) {
            logger.catching(e);
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);

        } catch (final IOException e) {
            logger.catching(e);
            final WebServerException exc = new WebServerException("Received I/O exception when deleting file " + relativePath, e);
            throw logger.throwing(exc);

        } catch (final IllegalArgumentException e) {
            logger.catching(e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
