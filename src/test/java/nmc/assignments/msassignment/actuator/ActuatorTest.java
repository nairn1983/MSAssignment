package nmc.assignments.msassignment.actuator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ActuatorTest {
    @Autowired
    private MockMvc mvc;

    @ParameterizedTest
    @ValueSource(strings = {"health", "info", "prometheus"})
    public void actuatorRequestShouldBeUnauthorized_whenNotAuthenticated(final String endpoint) throws Exception {
        mvc.perform(get("/actuator/" + endpoint))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void healthStatusShouldBeUp_whenAuthenticated() throws Exception {
        mvc.perform(get("/actuator/health")
                .with(user("TEST_USER").password("ChangeMe")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    public void infoResponseShouldBeEmptyByDefault() throws Exception {
        mvc.perform(get("/actuator/info")
                .with(user("TEST_USER").password("ChangeMe")))
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.content().string("{}"));
    }
}
