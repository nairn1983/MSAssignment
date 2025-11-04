package nmc.assignments.msassignment.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class StatusControllerTest {
    @Autowired
    private MockMvc mvc;

    @Test
    public void statusShouldBeOk() throws Exception {
        mvc.perform(get("/status"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("OK"));
    }
}
