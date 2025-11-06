package nmc.assignments.msassignment.service;

import nmc.assignments.msassignment.entity.UploadedFileInformation;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;

public interface FileUploadService {
    UploadedFileInformation uploadFile(String relativePath, MultipartFile file) throws IOException;

    URI createUriFromUploadedFile(UploadedFileInformation uploadedFileInformation, String relativePath);
}
