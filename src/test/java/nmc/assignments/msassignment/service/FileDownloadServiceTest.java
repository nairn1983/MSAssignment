package nmc.assignments.msassignment.service;

import nmc.assignments.msassignment.entity.DownloadedFileInformation;
import nmc.assignments.msassignment.service.impl.FileDownloadServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FileDownloadServiceTest {
    @InjectMocks
    private FileDownloadServiceImpl fileDownloadService;

    @Mock
    private PathSanitationService pathSanitationService;

    @Mock
    private ResourceService resourceService;

    @Mock
    private StorageLocationService storageLocationService;

    private MockedStatic<Files> files;

    @BeforeEach
    public void setUp() {
        files = Mockito.mockStatic(Files.class);
    }

    @AfterEach
    public void tearDown() {
        files.close();
    }

    @ParameterizedTest
    @CsvSource({
        "file_to_download.txt,text/plain",
        "image.jpg,image/jpeg",
        "octet_stream_example,"
    })
    public void shouldDownloadFileUsingRelativePath(
        final String relativePath,
        final String mediaType) throws Exception {

        // Arrange
        final String fullPath = "/tmp/" + relativePath;
        when(storageLocationService.getAbsolutePath(relativePath)).thenReturn(fullPath);

        final Path path = mock(Path.class);
        when(pathSanitationService.sanitiseFile(eq(fullPath), eq(relativePath))).thenReturn(path);

        final Path fileName = mock(Path.class);
        when(path.getFileName()).thenReturn(fileName);
        when(fileName.toString()).thenReturn(relativePath);

        final Resource resource = mock(Resource.class);
        when(resourceService.getUrlResourceFromPath(eq(path), eq(relativePath))).thenReturn(resource);

        final String contentType = mediaType != null ? mediaType : "application/octet-stream";
        files.when(() -> Files.probeContentType(eq(path))).thenReturn(contentType);

        final long fileSize = 100L;
        files.when(() -> Files.size(path)).thenReturn(fileSize);

        // Act
        final DownloadedFileInformation downloadedFileInformation = fileDownloadService.downloadFile(relativePath);

        // Assert
        final DownloadedFileInformation expectedFileInformation = new DownloadedFileInformation(
            contentType,
            relativePath,
            resource,
            fileSize);
        assertEquals(expectedFileInformation, downloadedFileInformation);
    }
}
