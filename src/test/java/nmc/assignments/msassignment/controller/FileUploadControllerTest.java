package nmc.assignments.msassignment.controller;

import nmc.assignments.msassignment.controller.file.FileUploadController;
import nmc.assignments.msassignment.entity.UploadedFileInformation;
import nmc.assignments.msassignment.service.FileUploadService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.server.WebServerException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileAlreadyExistsException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FileUploadControllerTest {
    @InjectMocks
    private FileUploadController fileUploadController;

    @Mock
    private FileUploadService fileUploadService;

    @ParameterizedTest
    @ValueSource(strings = {
        "file_to_upload.txt",
        "subdir/file_to_upload.txt",
        "../",
        ""
    })
    public void shouldRemoveLeadingSlashesBeforeCallingService(final String expectedFilepath) throws Exception {
        // Arrange
        final String filepath = "/" + expectedFilepath;
        final MultipartFile file = mock(MultipartFile.class);

        final UploadedFileInformation uploadedFileInformation = mock(UploadedFileInformation.class);
        when(fileUploadService.uploadFile(eq(expectedFilepath), eq(file)))
            .thenReturn(uploadedFileInformation);

        final URI locationUri = mock(URI.class);
        when(fileUploadService.createUriFromUploadedFile(eq(uploadedFileInformation), eq(expectedFilepath)))
            .thenReturn(locationUri);

        // Act
        fileUploadController.uploadFile(filepath, file);

        // Assert
        verify(fileUploadService).uploadFile(eq(expectedFilepath), eq(file));
        verify(fileUploadService).createUriFromUploadedFile(eq(uploadedFileInformation), eq(expectedFilepath));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "file_to_upload.txt",
        "subdir/file_to_upload.txt",
        "../",
        ""
    })
    public void shouldReturnHttp201WhenFilepathIsValid(final String filepath) throws Exception {
        // Arrange
        final MultipartFile file = mock(MultipartFile.class);

        final UploadedFileInformation uploadedFileInformation = mock(UploadedFileInformation.class);
        when(fileUploadService.uploadFile(eq(filepath), eq(file)))
            .thenReturn(uploadedFileInformation);

        final URI locationUri = mock(URI.class);
        when(fileUploadService.createUriFromUploadedFile(eq(uploadedFileInformation), eq(filepath)))
            .thenReturn(locationUri);

        // Act
        final ResponseEntity<?> responseEntity = fileUploadController.uploadFile(filepath, file);

        // Assert
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertEquals(uploadedFileInformation, responseEntity.getBody());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "file_to_upload.txt",
        "subdir/file_to_upload.txt",
        "../",
        ""
    })
    public void shouldReturnHttp409WhenFileAlreadyExists(final String filepath) throws Exception {
        // Arrange
        final MultipartFile file = mock(MultipartFile.class);
        doThrow(FileAlreadyExistsException.class).when(fileUploadService)
            .uploadFile(eq(filepath), eq(file));

        // Act
        final ResponseEntity<?> responseEntity = fileUploadController.uploadFile(filepath, file);

        // Assert
        assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode());
        assertEquals("The file " + filepath + " already exists", responseEntity.getBody());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "file_to_upload.txt",
        "subdir/file_to_upload.txt",
        "../",
        ""
    })
    public void shouldReturnHttp403WhenServiceRaisesIllegalArgumentException(final String filepath) throws Exception {
        // Arrange
        final MultipartFile file = mock(MultipartFile.class);
        doThrow(IllegalArgumentException.class).when(fileUploadService)
            .uploadFile(eq(filepath), eq(file));

        // Act
        final ResponseEntity<?> responseEntity = fileUploadController.uploadFile(filepath, file);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "file_to_upload.txt",
        "subdir/file_to_upload.txt",
        "../",
        ""
    })
    public void shouldThrowWebServerExceptionWhenServiceRaisesIOException(final String filepath) throws Exception {
        // Arrange
        final MultipartFile file = mock(MultipartFile.class);
        doThrow(IOException.class).when(fileUploadService)
            .uploadFile(eq(filepath), eq(file));

        // Act, assert
        assertThrows(WebServerException.class, () -> fileUploadController.uploadFile(filepath, file));
    }
}
