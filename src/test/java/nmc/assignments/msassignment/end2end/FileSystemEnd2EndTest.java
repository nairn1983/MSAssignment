package nmc.assignments.msassignment.end2end;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Path;

import static nmc.assignments.msassignment.config.FileServicesConfig.ENDPOINTS_ROOT;
import static org.hamcrest.Matchers.endsWith;
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
    static Path tempDir;

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("storage.location", () -> tempDir.toString());
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

    @Test
    public void shouldHandleUploadOfSingleFile() throws Exception {
        // Upload -- should be OK
        final MockMultipartFile firstFile = new MockMultipartFile("file", "testFile.txt", "text/plain", "Content of first file".getBytes());

        mvc.perform(multipart(ENDPOINTS_ROOT + "/upload/subdir1/uploadedFile.txt")
                .file(firstFile)
                .with(httpBasic(USER, PASS)))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", endsWith(ENDPOINTS_ROOT + "/download/subdir1/uploadedFile.txt")));

        // Upload to same location -- should return conflict
        final MockMultipartFile secondFile = new MockMultipartFile("file", "secondFile.txt", "text/plain", "Content of second file".getBytes());

        mvc.perform(multipart(ENDPOINTS_ROOT + "/upload/subdir1/uploadedFile.txt")
                .file(secondFile)
                .with(httpBasic(USER, PASS)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$").value("The file subdir1/uploadedFile.txt already exists"));

        // List -- should return uploaded file
        mvc.perform(get(ENDPOINTS_ROOT + "/list")
                .with(httpBasic(USER, PASS)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value("/subdir1/uploadedFile.txt"));

        // Download -- should return content of first file
        mvc.perform(get(ENDPOINTS_ROOT + "/download/subdir1/uploadedFile.txt")
                .with(httpBasic(USER, PASS)))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", "attachment; filename=\"uploadedFile.txt\""))
            .andExpect(header().string("Content-Type", "text/plain"))
            .andExpect(content().string("Content of first file"));

        // Delete -- should be successful if filename is correct
        mvc.perform(delete(ENDPOINTS_ROOT + "/delete/subdir1/uploadedFile.txt")
            .with(httpBasic(USER, PASS)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").doesNotExist());

        // Download -- should return not found
        mvc.perform(get(ENDPOINTS_ROOT + "/download/subdir1/uploadedFile.txt")
                .with(httpBasic(USER, PASS)))
            .andExpect(status().isNotFound());

        // Upload -- should be possible to upload the second file to the same location
        mvc.perform(multipart(ENDPOINTS_ROOT + "/upload/subdir1/uploadedFile.txt")
                .file(secondFile)
                .with(httpBasic(USER, PASS)))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", endsWith(ENDPOINTS_ROOT + "/download/subdir1/uploadedFile.txt")));

        // Download -- should return content of second file
        mvc.perform(get(ENDPOINTS_ROOT + "/download/subdir1/uploadedFile.txt")
                .with(httpBasic(USER, PASS)))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", "attachment; filename=\"uploadedFile.txt\""))
            .andExpect(header().string("Content-Type", "text/plain"))
            .andExpect(content().string("Content of second file"));
    }
}
