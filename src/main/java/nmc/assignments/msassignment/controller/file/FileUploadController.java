package nmc.assignments.msassignment.controller.file;

import io.swagger.v3.oas.annotations.Parameter;
import nmc.assignments.msassignment.entity.FileInformation;
import nmc.assignments.msassignment.service.FileUploadService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.WebServerException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileAlreadyExistsException;

import static nmc.assignments.msassignment.config.FileServicesConfig.ENDPOINTS_ROOT;

@RestController
public class FileUploadController {
    private static final Logger logger = LogManager.getLogger(FileUploadController.class);

    @Autowired
    private FileUploadService fileUploadService;

    @PostMapping(value = ENDPOINTS_ROOT + "/upload/{*filepath}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFile(@PathVariable String filepath, @RequestPart("file") MultipartFile file) {
        try {
            final FileInformation fileInformation = fileUploadService.uploadFile(filepath, file);

            final URI locationUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(ENDPOINTS_ROOT + "/download/")
                .path(filepath)
                .build()
                .toUri();

            return ResponseEntity.created(locationUri)
                .body(fileInformation);

        } catch (final FileAlreadyExistsException e) {
            logger.catching(e);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("The file " + e.getMessage() + " already exists");

        } catch (final IllegalArgumentException e) {
            logger.catching(e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);

        } catch (final IOException e) {
            logger.catching(e);
            final WebServerException exc = new WebServerException("Received I/O exception when downloading file " + filepath, e);
            throw logger.throwing(exc);

        }
    }
}
