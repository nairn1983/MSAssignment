package nmc.assignments.msassignment.service;

import nmc.assignments.msassignment.service.impl.FileDeletionServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FileDeletionServiceTest {
    @InjectMocks
    private FileDeletionServiceImpl fileDeletionService;

    @Mock
    private PathSanitationService pathSanitationService;

    @Mock
    private StorageLocationService storageLocationService;

    private MockedStatic<Files> files;

    @BeforeEach
    public void setUp() {
        files = mockStatic(Files.class);
    }

    @AfterEach
    public void tearDown() {
        files.close();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "file_to_delete.txt",
        "subdir/file_to_delete.txt"
    })
    public void shouldDeleteUsingRelativePath(final String relativePath) throws Exception {
        // Arrange
        final String fullPath = "/tmp/" + relativePath;
        when(storageLocationService.getAbsolutePath(relativePath)).thenReturn(fullPath);

        final Path path = mock(Path.class);
        when(pathSanitationService.sanitiseFile(eq(fullPath), eq(relativePath))).thenReturn(path);

        // Act
        fileDeletionService.deleteFile(relativePath);

        // Assert
        files.verify(() -> Files.delete(path));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "file_to_delete.txt",
        "subdir/file_to_delete.txt"
    })
    public void shouldRemoveLeadingSlashesBeforeDeletingFile(final String expectedFilepath) throws IOException {
        // Arrange
        final String filepath = "/" + expectedFilepath;
        final String fullPath = "/tmp/" + expectedFilepath;
        when(storageLocationService.getAbsolutePath(expectedFilepath)).thenReturn(fullPath);

        final Path path = mock(Path.class);
        when(pathSanitationService.sanitiseFile(eq(fullPath), eq(expectedFilepath))).thenReturn(path);

        // Act
        fileDeletionService.deleteFile(filepath);

        // Assert
        files.verify(() -> Files.delete(path));
    }
}
