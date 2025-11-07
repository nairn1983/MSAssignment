package nmc.assignments.msassignment.controller;

import nmc.assignments.msassignment.controller.file.FileDownloadController;
import nmc.assignments.msassignment.entity.DownloadedFileInformation;
import nmc.assignments.msassignment.service.FileDownloadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.server.WebServerException;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FileDownloadControllerTest {
    @InjectMocks
    private FileDownloadController fileDownloadController;

    @Mock
    private FileDownloadService fileDownloadService;

    @BeforeEach
    public void setup() {
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "file_to_delete.txt",
        "subdir/file_to_delete.txt",
        "../",
        ""
    })
    public void shouldReturnHttp200WhenFilepathIsValid(final String filepath) throws Exception {
        // Arrange
        final DownloadedFileInformation downloadedFileInformation = mockDownloadedFileInformation(filepath);
        when(fileDownloadService.downloadFile(eq(filepath))).thenReturn(downloadedFileInformation);

        // Act
        final ResponseEntity<?> responseEntity = fileDownloadController.downloadFile(filepath);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(downloadedFileInformation.getSize(), responseEntity.getHeaders().getContentLength());
        assertEquals(downloadedFileInformation.getFilename(), responseEntity.getHeaders().getContentDisposition().getFilename());
        assertEquals(MediaType.parseMediaType(downloadedFileInformation.getContentType()), responseEntity.getHeaders().getContentType());
        assertEquals(downloadedFileInformation.getResource(), responseEntity.getBody());
    }

    private DownloadedFileInformation mockDownloadedFileInformation(final String filepath) {
        final DownloadedFileInformation downloadedFileInformation = mock(DownloadedFileInformation.class);
        when(downloadedFileInformation.getContentType()).thenReturn("text/dummy");
        when(downloadedFileInformation.getFilename()).thenReturn(filepath);
        when(downloadedFileInformation.getSize()).thenReturn(100L);

        final Resource resource = mock(Resource.class);
        when(downloadedFileInformation.getResource()).thenReturn(resource);

        return downloadedFileInformation;
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "file_to_download.txt",
        "subdir/file_to_download.txt",
        "../",
        ""
    })
    public void shouldRemoveLeadingSlashesBeforeCallingService(final String expectedFilepath) throws Exception {
        // Arrange
        final String filepath = "/" + expectedFilepath;

        final DownloadedFileInformation downloadedFileInformation = mock(DownloadedFileInformation.class);
        when(fileDownloadService.downloadFile(eq(expectedFilepath))).thenReturn(downloadedFileInformation);
        when(downloadedFileInformation.getContentType()).thenReturn("text/dummy");

        // Act
        fileDownloadController.downloadFile(filepath);

        // Assert
        verify(fileDownloadService).downloadFile(eq(expectedFilepath));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "file_to_delete.txt",
        "subdir/file_to_delete.txt",
        "../",
        ""
    })
    public void shouldThrowWebServerExceptionWhenServiceRaisesIOException(final String filepath) throws Exception {
        // Arrange
        doThrow(IOException.class).when(fileDownloadService)
            .downloadFile(filepath);

        // Act, assert
        assertThrows(WebServerException.class, () -> fileDownloadController.downloadFile(filepath));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "file_to_download.txt",
        "subdir/file_to_download.txt",
        "../",
        ""
    })
    public void shouldReturnHttp404WhenServiceRaisesFileNotFoundException(final String filepath) throws Exception {
        // Arrange
        doThrow(FileNotFoundException.class).when(fileDownloadService)
            .downloadFile(eq(filepath));

        // Act
        final ResponseEntity<?> responseEntity = fileDownloadController.downloadFile(filepath);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "file_to_download.txt",
        "subdir/file_to_download.txt",
        "../",
        ""
    })
    public void shouldReturnHttp400WhenServiceRaisesIllegalArgumentException(final String filepath) throws Exception {
        // Arrange
        final String errorMessage = "Could not download file " + filepath;
        doThrow(new IllegalArgumentException(errorMessage)).when(fileDownloadService)
            .downloadFile(eq(filepath));

        // Act
        final ResponseEntity<?> responseEntity = fileDownloadController.downloadFile(filepath);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(errorMessage, responseEntity.getBody());
    }
}
