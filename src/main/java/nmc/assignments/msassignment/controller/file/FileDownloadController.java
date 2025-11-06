package nmc.assignments.msassignment.controller.file;

import nmc.assignments.msassignment.entity.DownloadedFileInformation;
import nmc.assignments.msassignment.service.FileDownloadService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.WebServerException;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileNotFoundException;
import java.io.IOException;

import static nmc.assignments.msassignment.config.FileServicesConfig.ENDPOINTS_ROOT;

@RestController
public class FileDownloadController {
    private static final Logger logger = LogManager.getLogger(FileDownloadController.class);

    @Autowired
    private FileDownloadService fileDownloadService;

    @GetMapping(ENDPOINTS_ROOT + "/download/{*filepath}")
    public ResponseEntity<?> downloadFile(@PathVariable final String filepath) {
        final String relativePath = filepath.startsWith("/") ? filepath.substring(1) : filepath;
        logger.info("Downloading file {}", relativePath);

        try {
            final DownloadedFileInformation fileInformation = fileDownloadService.downloadFile(relativePath);

            final String contentType = fileInformation.getContentType();
            final MediaType mediaType = MediaType.parseMediaType(contentType);
            final String filename = fileInformation.getFilename();
            final long fileSize = fileInformation.getSize();
            final Resource resource = fileInformation.getResource();

            logger.info("Ready to download file {}", relativePath);
            return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache())
                .contentLength(fileSize)
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                    ContentDisposition.attachment()
                        .filename(filename)
                        .build()
                        .toString())
                .body(resource);

        } catch (final FileNotFoundException e) {
            logger.catching(e);
            logger.warn("File {} does not exist or is not readable", relativePath);
            return ResponseEntity.notFound().build();

        } catch (final IOException e) {
            logger.catching(e);
            final WebServerException exc = new WebServerException("Received I/O exception when downloading file " + relativePath, e);
            throw logger.throwing(exc);

        } catch (final IllegalArgumentException e) {
            logger.catching(e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
        }
    }
}
