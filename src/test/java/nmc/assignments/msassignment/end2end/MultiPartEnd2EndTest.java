package nmc.assignments.msassignment.end2end;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MultiPartEnd2EndTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @TempDir
    private static Path storageRoot;

    @DynamicPropertySource
    private static void dynamicProperties(final DynamicPropertyRegistry registry) {
        registry.add("storage.location", () -> storageRoot.toString());
        registry.add("spring.servlet.multipart.max-file-size", () -> "1KB");
        registry.add("spring.servlet.multipart.max-request-size", () -> "1KB");
    }

    @Test
    public void shouldHandlePayloadTooLarge() throws Exception {
        // Upload a file that is too large for the 1KB limit -- this should result in an HTTP 413
        final byte[] bytes = new byte[2048];

        final ByteArrayResource resource = new ByteArrayResource(bytes) {
            @Override
            public String getFilename() {
                return "largeFile.bin";
            }
        };

        final MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", resource);

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBasicAuth("TEST_USER", "ChangeMe");

        final HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
        final ResponseEntity<String> response = restTemplate.postForEntity("/file/upload/largeFile.bin", request, String.class);

        assertEquals(HttpStatus.PAYLOAD_TOO_LARGE, response.getStatusCode());
    }
}
