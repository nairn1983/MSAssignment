package nmc.assignments.msassignment.end2end;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static nmc.assignments.msassignment.config.FileServicesConfig.ENDPOINTS_ROOT;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class FileSystemEnd2EndTest {
    private static final String USER = "TEST_USER";
    private static final String PASS = "ChangeMe";

    @TempDir
    private static Path storageRoot;

    @DynamicPropertySource
    private static void dynamicProperties(final DynamicPropertyRegistry registry) {
        registry.add("storage.location", () -> storageRoot.toString());
    }

    @Autowired
    private MockMvc mvc;

    @Test
    public void fileListShouldBeEmptyByDefault() throws Exception {
        mvc.perform(get(ENDPOINTS_ROOT + "/list")
                .with(httpBasic(USER, PASS)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isEmpty());
    }

    @ParameterizedTest
    @CsvSource({
        "subdir1/uploadedFile.txt,uploadedFile.txt",
        // It is unspecified how spaces in filenames should be handled -- we allow the filename to be handled without conversion in this case
        "subdir1/uploaded File.txt,uploaded File.txt",
        "sub dir1/uploadedFile.txt,uploadedFile.txt",
        "sub dir1/uploaded File.txt,uploaded File.txt"
    })
    public void shouldHandleUploadOfSingleFile(final String relativePath, final String filename) throws Exception {
        // Upload -- should be OK
        final MockMultipartFile firstFile = new MockMultipartFile("file", "testFile.txt", "text/plain", "Content of first file".getBytes());

        mvc.perform(multipart(ENDPOINTS_ROOT + "/upload/" + relativePath)
                .file(firstFile)
                .with(httpBasic(USER, PASS)))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", endsWith(ENDPOINTS_ROOT + "/download/" + relativePath.replace(" ", "%20"))))
            .andExpect(jsonPath("$.path", equalTo(relativePath)));

        // Upload to same location -- should return conflict
        final MockMultipartFile secondFile = new MockMultipartFile("file", "secondFile.txt", "text/plain", "Content of second file".getBytes());

        mvc.perform(multipart(ENDPOINTS_ROOT + "/upload/" + relativePath)
                .file(secondFile)
                .with(httpBasic(USER, PASS)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$").value("The file " + relativePath + " already exists"));

        // List -- should return uploaded file
        mvc.perform(get(ENDPOINTS_ROOT + "/list")
                .with(httpBasic(USER, PASS)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value("/" + relativePath));

        // Download -- should return content of first file
        mvc.perform(get(ENDPOINTS_ROOT + "/download/" + relativePath)
                .with(httpBasic(USER, PASS)))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", "attachment; filename=\"" + filename + "\""))
            .andExpect(header().string("Content-Type", "text/plain"))
            .andExpect(content().string("Content of first file"));

        // Delete -- should be successful if filename is correct
        mvc.perform(delete(ENDPOINTS_ROOT + "/delete/" + relativePath)
                .with(httpBasic(USER, PASS)))
            .andExpect(status().isNoContent())
            .andExpect(jsonPath("$").doesNotExist());

        // Download -- should return not found
        mvc.perform(get(ENDPOINTS_ROOT + "/download/" + relativePath)
                .with(httpBasic(USER, PASS)))
            .andExpect(status().isNotFound());

        // Upload -- should be possible to upload the second file to the same location
        mvc.perform(multipart(ENDPOINTS_ROOT + "/upload/" + relativePath)
                .file(secondFile)
                .with(httpBasic(USER, PASS)))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", endsWith(ENDPOINTS_ROOT + "/download/" + relativePath.replace(" ", "%20"))))
            .andExpect(jsonPath("$.path", equalTo(relativePath)));

        // Download -- should return content of second file
        mvc.perform(get(ENDPOINTS_ROOT + "/download/" + relativePath)
                .with(httpBasic(USER, PASS)))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", "attachment; filename=\"" + filename + "\""))
            .andExpect(header().string("Content-Type", "text/plain"))
            .andExpect(content().string("Content of second file"));

        // Delete -- ensure that the file is deleted
        mvc.perform(delete(ENDPOINTS_ROOT + "/delete/" + relativePath)
                .with(httpBasic(USER, PASS)))
            .andExpect(status().isNoContent())
            .andExpect(jsonPath("$").doesNotExist());

        // List -- check that the storage filesystem is empty
        mvc.perform(get(ENDPOINTS_ROOT + "/list")
                .with(httpBasic(USER, PASS)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    public void shouldHandleUnauthorisedRequests() throws Exception {
        final String unknownUser = "UnknownUser";
        final String unknownPass = "UnknownPass";

        // Delete
        mvc.perform(delete(ENDPOINTS_ROOT + "/delete/subdir1/uploadedFile.txt")
                .with(httpBasic(unknownUser, unknownPass)))
            .andExpect(status().isUnauthorized());

        // Download
        mvc.perform(get(ENDPOINTS_ROOT + "/download/subdir1/uploadedFile.txt")
                .with(httpBasic(unknownUser, unknownPass)))
            .andExpect(status().isUnauthorized());

        // List
        mvc.perform(get(ENDPOINTS_ROOT + "/list")
                .with(httpBasic(unknownUser, unknownPass)))
            .andExpect(status().isUnauthorized());

        // Upload
        final MockMultipartFile firstFile = new MockMultipartFile("file", "testFile.txt", "text/plain", "Content of first file".getBytes());

        mvc.perform(multipart(ENDPOINTS_ROOT + "/upload/subdir1/uploadedFile.txt")
                .file(firstFile)
                .with(httpBasic(unknownUser, unknownPass)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void shouldHandleNoUserRequests() throws Exception {
        // Delete
        mvc.perform(delete(ENDPOINTS_ROOT + "/delete/subdir1/uploadedFile.txt"))
            .andExpect(status().isUnauthorized());

        // Download
        mvc.perform(get(ENDPOINTS_ROOT + "/download/subdir1/uploadedFile.txt"))
            .andExpect(status().isUnauthorized());

        // List
        mvc.perform(get(ENDPOINTS_ROOT + "/list"))
            .andExpect(status().isUnauthorized());

        // Upload
        final MockMultipartFile firstFile = new MockMultipartFile("file", "testFile.txt", "text/plain", "Content of first file".getBytes());

        mvc.perform(multipart(ENDPOINTS_ROOT + "/upload/subdir1/uploadedFile.txt")
                .file(firstFile))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void shouldPreventPathTraversalToRootOfFilesystem() throws Exception {
        // Upload -- prevent path traversal to the root directory
        final MockMultipartFile firstFile = new MockMultipartFile("file", "testFile.txt", "text/plain", "Content of first file".getBytes());

        mvc.perform(multipart(ENDPOINTS_ROOT + "/upload//uploadedFile.txt")
                .file(firstFile)
                .with(httpBasic(USER, PASS)))
            .andExpect(status().isCreated())
            // The file is uploaded to the storage directory -- i.e., the root is treated as the storage directory
            .andExpect(header().string("Location", endsWith(ENDPOINTS_ROOT + "/download/uploadedFile.txt")));

        // List -- should return uploaded file
        mvc.perform(get(ENDPOINTS_ROOT + "/list")
                .with(httpBasic(USER, PASS)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value("/uploadedFile.txt"));

        // Download -- should download the uploaded file from the storage root
        mvc.perform(get(ENDPOINTS_ROOT + "/download//uploadedFile.txt")
                .with(httpBasic(USER, PASS)))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", "attachment; filename=\"uploadedFile.txt\""))
            .andExpect(header().string("Content-Type", "text/plain"))
            .andExpect(content().string("Content of first file"));

        // Delete -- prevent path traversal to the root directory
        mvc.perform(delete(ENDPOINTS_ROOT + "/delete//uploadedFile.txt")
                .with(httpBasic(USER, PASS)))
            // This will be successful because the file at the root of the storage directory is deleted
            .andExpect(status().isNoContent())
            .andExpect(jsonPath("$").doesNotExist());
    }

    @Test
    public void shouldPreventPathTraversalToRootOfFilesystemUsingEncodedUrls() throws Exception {
        // Delete -- prevent path traversal to the storage directory's parent
        mvc.perform(delete(ENDPOINTS_ROOT + "/delete/%2FuploadedFile.txt")
                .with(httpBasic(USER, PASS)))
            // Tomcat prevents the request above from reaching the delete controller
            .andExpect(status().isBadRequest());

        // Download -- prevent path traversal to the storage directory's parent
        mvc.perform(get(ENDPOINTS_ROOT + "/download/%2FuploadedFile.txt")
                .with(httpBasic(USER, PASS)))
            .andExpect(status().isBadRequest());

        // Upload -- prevent path traversal to the storage directory's parent
        final MockMultipartFile firstFile = new MockMultipartFile("file", "testFile.txt", "text/plain", "Content of first file".getBytes());

        mvc.perform(multipart(ENDPOINTS_ROOT + "/upload/%2FuploadedFile.txt")
                .file(firstFile)
                .with(httpBasic(USER, PASS)))
            .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        // We prevent path traversals outside the storage filesystem
        "../uploadedFile.txt",
        // We also prevent path traversals that end up within the storage filesystem
        "subdir1/../uploadedFile.txt"
    })
    public void shouldPreventPathTraversalOutsideStorageFilesystem(final String relativePath) throws Exception {
        // Delete -- prevent path traversal to the storage directory's parent
        mvc.perform(delete(ENDPOINTS_ROOT + "/delete/" + relativePath)
                .with(httpBasic(USER, PASS)))
            // Tomcat prevents the request above from reaching the delete controller
            .andExpect(status().isBadRequest());

        // Download -- prevent path traversal to the storage directory's parent
        mvc.perform(get(ENDPOINTS_ROOT + "/download/" + relativePath)
                .with(httpBasic(USER, PASS)))
            .andExpect(status().isBadRequest());

        // Upload -- prevent path traversal to the storage directory's parent
        final MockMultipartFile firstFile = new MockMultipartFile("file", "testFile.txt", "text/plain", "Content of first file".getBytes());

        mvc.perform(multipart(ENDPOINTS_ROOT + "/upload/" + relativePath)
                .file(firstFile)
                .with(httpBasic(USER, PASS)))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldPreventSpaceEncodedPaths() throws Exception {
        // Delete -- prevent path traversal to the storage directory's parent
        mvc.perform(delete(ENDPOINTS_ROOT + "/delete/uploaded%20File.txt")
                .with(httpBasic(USER, PASS)))
            .andExpect(status().isBadRequest());

        // Download -- prevent path traversal to the storage directory's parent
        mvc.perform(get(ENDPOINTS_ROOT + "/download/uploaded%20File.txt")
                .with(httpBasic(USER, PASS)))
            .andExpect(status().isBadRequest());

        // Upload -- prevent path traversal to the storage directory's parent
        final MockMultipartFile firstFile = new MockMultipartFile("file", "testFile.txt", "text/plain", "Content of first file".getBytes());

        mvc.perform(multipart(ENDPOINTS_ROOT + "/upload/uploaded%20File.txt")
                .file(firstFile)
                .with(httpBasic(USER, PASS)))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldAllowMultipleUploadsOfTheSameFile() throws Exception {
        final MockMultipartFile firstFile = new MockMultipartFile("file", "testFile.txt", "text/plain", "Content of first file".getBytes());

        // Upload file to one location -- should return HTTP 201
        mvc.perform(multipart(ENDPOINTS_ROOT + "/upload/uploadedFile1.txt")
                .file(firstFile)
                .with(httpBasic(USER, PASS)))
            .andExpect(status().isCreated());

        // Upload file to a different location -- should also return HTTP 201
        mvc.perform(multipart(ENDPOINTS_ROOT + "/upload/uploadedFile2.txt")
                .file(firstFile)
                .with(httpBasic(USER, PASS)))
            .andExpect(status().isCreated());

        // Delete both files
        mvc.perform(delete(ENDPOINTS_ROOT + "/delete/uploadedFile1.txt")
                .with(httpBasic(USER, PASS)))
            .andExpect(status().isNoContent())
            .andExpect(jsonPath("$").doesNotExist());

        mvc.perform(delete(ENDPOINTS_ROOT + "/delete/uploadedFile2.txt")
                .with(httpBasic(USER, PASS)))
            .andExpect(status().isNoContent())
            .andExpect(jsonPath("$").doesNotExist());

        // List -- confirm that the storage filesystem is empty
        mvc.perform(get(ENDPOINTS_ROOT + "/list")
                .with(httpBasic(USER, PASS)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    public void shouldPreventConcurrentUploadsToSameFileLocation() throws Exception {
        // Create two different files -- we will attempt to handle a race condition by uploading these concurrently
        // and demonstrating that only the first file that was processed is uploaded
        final MockMultipartFile firstFile = new MockMultipartFile("file", "testFile.txt", "text/plain", "Content of first file".getBytes());
        final MockMultipartFile secondFile = new MockMultipartFile("file", "testFile.txt", "text/plain", "Content of second file".getBytes());

        final ExecutorService executorService = Executors.newFixedThreadPool(2);
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        // Create a callable that will wait until the countdown latch is triggered before attempting to upload each respective file
        final Callable<ResultActions> callableOnFirstFile = () -> {
            countDownLatch.await();
            return mvc.perform(multipart(ENDPOINTS_ROOT + "/upload/uploadedFile.txt")
                .file(firstFile)
                .with(httpBasic(USER, PASS)));
        };
        final Callable<ResultActions> callableOnSecondFile = () -> {
            countDownLatch.await();
            return mvc.perform(multipart(ENDPOINTS_ROOT + "/upload/uploadedFile.txt")
                .file(secondFile)
                .with(httpBasic(USER, PASS)));
        };

        // Submit both callables to the thread pool
        final Future<ResultActions> firstFileFuture = executorService.submit(callableOnFirstFile);
        final Future<ResultActions> secondFileFuture = executorService.submit(callableOnSecondFile);

        // Trigger both callables by releasing the countdown latch
        countDownLatch.countDown();

        final int firstFileStatus = firstFileFuture.get().andReturn().getResponse().getStatus();
        final int secondFileStatus = secondFileFuture.get().andReturn().getResponse().getStatus();

        executorService.shutdown();

        // We expect that either an HTTP 201 is returned for the first file and an HTTP 409 for the second or vice versa
        final boolean firstFileWasUploaded = firstFileStatus == HttpStatus.CREATED.value() && secondFileStatus == HttpStatus.CONFLICT.value();
        final boolean secondFileWasUploaded = firstFileStatus == HttpStatus.CONFLICT.value() && secondFileStatus == HttpStatus.CREATED.value();

        assertTrue(firstFileWasUploaded || secondFileWasUploaded);

        final Path resolve = storageRoot.resolve("uploadedFile.txt");
        final byte[] bytes = Files.readAllBytes(resolve);
        final String output = new String(bytes);

        final String expectedOutput = firstFileWasUploaded ? "Content of first file" : "Content of second file";
        assertEquals(expectedOutput, output);

        // Delete the file
        mvc.perform(delete(ENDPOINTS_ROOT + "/delete/uploadedFile.txt")
                .with(httpBasic(USER, PASS)))
            .andExpect(status().isNoContent())
            .andExpect(jsonPath("$").doesNotExist());

        // List -- confirm that the storage filesystem is empty
        mvc.perform(get(ENDPOINTS_ROOT + "/list")
                .with(httpBasic(USER, PASS)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    public void shouldPreventConcurrentDeletionOfTheSameFile() throws Exception {
        // Upload a file -- attempt to handle a race condition by deleting it through two concurrent processes.
        final MockMultipartFile firstFile = new MockMultipartFile("file", "testFile.txt", "text/plain", "Content of first file".getBytes());

        mvc.perform(multipart(ENDPOINTS_ROOT + "/upload/uploadedFile.txt")
            .file(firstFile)
            .with(httpBasic(USER, PASS)));

        final ExecutorService executorService = Executors.newFixedThreadPool(2);
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        // Create a callable that will wait until the countdown latch is triggered before attempting to delete the file
        final Callable<ResultActions> callable1 = () -> {
            countDownLatch.await();
            return mvc.perform(delete(ENDPOINTS_ROOT + "/delete/uploadedFile.txt")
                .with(httpBasic(USER, PASS)));
        };
        final Callable<ResultActions> callable2 = () -> {
            countDownLatch.await();
            return mvc.perform(delete(ENDPOINTS_ROOT + "/delete/uploadedFile.txt")
                .with(httpBasic(USER, PASS)));
        };

        // Submit both callables to the thread pool
        final Future<ResultActions> firstDeleteFuture = executorService.submit(callable1);
        final Future<ResultActions> secondDeleteFuture = executorService.submit(callable2);

        // Trigger both callables by releasing the countdown latch
        countDownLatch.countDown();

        final int firstDeleteStatus = firstDeleteFuture.get().andReturn().getResponse().getStatus();
        final int secondDeleteStatus = secondDeleteFuture.get().andReturn().getResponse().getStatus();

        executorService.shutdown();

        assertTrue(firstDeleteStatus == HttpStatus.NO_CONTENT.value() || secondDeleteStatus == HttpStatus.NO_CONTENT.value());

        // List -- confirm that the storage filesystem is empty
        mvc.perform(get(ENDPOINTS_ROOT + "/list")
                .with(httpBasic(USER, PASS)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isEmpty());
    }
}
