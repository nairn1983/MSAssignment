package nmc.assignments.msassignment.controller;

import nmc.assignments.msassignment.controller.file.FileDeletionController;
import nmc.assignments.msassignment.service.FileDeletionService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.server.WebServerException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class FileDeletionControllerTest {
    @InjectMocks
    private FileDeletionController fileDeletionController;

    @Mock
    private FileDeletionService fileDeletionService;

    @ParameterizedTest
    @ValueSource(strings = {
        "file_to_delete.txt",
        "subdir/file_to_delete.txt",
        "../",
        ""
    })
    public void shouldReturnHttp204WhenFilepathIsValid(final String filepath) {
        // Act
        final ResponseEntity<?> responseEntity = fileDeletionController.deleteFile(filepath);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "file_to_delete.txt",
        "subdir/file_to_delete.txt",
        "../",
        ""
    })
    public void shouldRemoveLeadingSlashesBeforeCallingService(final String expectedFilepath) throws IOException {
        // Arrange
        final String filepath = "/" + expectedFilepath;

        // Act
        fileDeletionController.deleteFile(filepath);

        // Assert
        verify(fileDeletionService).deleteFile(eq(expectedFilepath));
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
        doThrow(IOException.class).when(fileDeletionService)
            .deleteFile(filepath);

        // Act, assert
        assertThrows(WebServerException.class, () -> fileDeletionController.deleteFile(filepath));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "file_to_delete.txt",
        "subdir/file_to_delete.txt",
        "../",
        ""
    })
    public void shouldReturnHttp400WhenServiceRaisesIllegalArgumentException(final String filepath) throws Exception {
        // Arrange
        final String errorMessage = "Could not delete file " + filepath;
        doThrow(new IllegalArgumentException(errorMessage)).when(fileDeletionService)
            .deleteFile(filepath);

        // Act
        final ResponseEntity<?> responseEntity = fileDeletionController.deleteFile(filepath);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(errorMessage, responseEntity.getBody());
    }
}
