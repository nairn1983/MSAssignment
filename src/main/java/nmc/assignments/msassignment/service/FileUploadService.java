package nmc.assignments.msassignment.service;

import nmc.assignments.msassignment.entity.FileInformation;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileUploadService {
    FileInformation uploadFile(String relativePath, MultipartFile file) throws IOException;
}
