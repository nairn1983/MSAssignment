package nmc.assignments.msassignment.service;

import nmc.assignments.msassignment.service.impl.FileListingServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FileListingServiceTest {
    @InjectMocks
    private FileListingServiceImpl fileListingService;

    @Mock
    private StorageLocationService storageLocationService;

    private MockedStatic<Files> files;

    @BeforeEach
    public void setup() {
        files = Mockito.mockStatic(Files.class);
    }

    @AfterEach
    public void teardown() {
        files.close();
    }

    @ParameterizedTest
    @MethodSource("directoryData")
    public void shouldListFilesWhenCalledByService(final List<String> filePaths) throws Exception {
        // Arrange
        when(storageLocationService.getStorageLocationPath()).thenReturn(mock(Path.class));

        final List<Path> paths = filePaths.stream()
            .map(filePath -> {
                final Path path = mock(Path.class);
                files.when(() -> Files.isDirectory(eq(path))).thenReturn(filePath.contains("/"));
                files.when(() -> Files.isRegularFile(eq(path))).thenReturn(!filePath.contains("/"));

                final Path internalPath = mock(Path.class);
                when(path.getFileName()).thenReturn(internalPath);

                final int index = filePath.indexOf("/");
                final String filename = index <= 0 ? filePath : filePath.substring(0, index);
                when(internalPath.toString()).thenReturn(filename);

                return path;
            }).toList();
        files.when(() -> Files.list(any(Path.class))).thenReturn(paths.stream());

        // Act
        final List<String> listedFiles = fileListingService.listAllFiles();

        // Assert
        final List<String> expectedFiles = filePaths.stream()
            .map(filePath -> "/" + filePath)
            .toList();
        assertTrue(expectedFiles.containsAll(listedFiles));
        assertTrue(listedFiles.containsAll(expectedFiles));
    }

    private static Stream<List<String>> directoryData() {
        return Stream.of(
            Collections.emptyList(),
            Collections.singletonList("file1.txt"),
            Arrays.asList("file1.txt", "file2.txt"));
    }

    @Test
    public void shouldThrowIOExceptionWhenRaisedByFiles() {
        // Arrange
        when(storageLocationService.getStorageLocationPath()).thenReturn(mock(Path.class));
        files.when(() -> Files.list(any(Path.class))).thenThrow(IOException.class);

        // Act, assert
        assertThrows(IOException.class, () -> fileListingService.listAllFiles());
    }
}
