package nmc.assignments.msassignment.controller;

import nmc.assignments.msassignment.controller.file.FileListingController;
import nmc.assignments.msassignment.service.FileListingService;
import org.junit.jupiter.api.Test;
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
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
public class FileListingControllerTest {
    @InjectMocks
    private FileListingController fileListingController;

    @Mock
    private FileListingService fileListingService;

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    public void shouldReturnHttp200WhenFilepathIsValid(final int numberOfFiles) throws Exception {
        // Arrange
        // Mock a list of filenames of size numberOfFiles, that the fileListingService will return
        final List<String> filenames = new ArrayList<>();
        for (int i = 0; i < numberOfFiles; i++) {
            filenames.add("file" + i);
        }
        doReturn(filenames).when(fileListingService).listAllFiles();

        // Act
        final ResponseEntity<List<String>> response = fileListingController.listAllFiles();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(filenames, response.getBody());
    }

    @Test
    public void shouldThrowWebServerExceptionWhenServiceRaisesIOException() throws Exception {
        // Arrange
        doThrow(IOException.class).when(fileListingService).listAllFiles();

        // Act, assert
        assertThrows(WebServerException.class, () -> fileListingController.listAllFiles());
    }
}
