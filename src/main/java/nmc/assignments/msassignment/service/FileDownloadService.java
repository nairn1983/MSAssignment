package nmc.assignments.msassignment.service;

import nmc.assignments.msassignment.entity.DownloadedFileInformation;

import java.io.IOException;

public interface FileDownloadService {
    DownloadedFileInformation downloadFile(String relativePath) throws IOException;
}
