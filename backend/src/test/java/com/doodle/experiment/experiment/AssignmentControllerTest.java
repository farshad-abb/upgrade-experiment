package com.doodle.experiment.experiment;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
class AssignmentControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void assignsAVariantAndKeepsIt() throws Exception {
        String url = "/api/visitors/" + UUID.randomUUID() + "/assignments";

        MvcResult first = mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.upgrade-button").isString())
                .andReturn();
        MvcResult second = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();

        assertThat(second.getResponse().getContentAsString()).isEqualTo(first.getResponse().getContentAsString());
    }

    @Test
    void rejectsAVisitorIdThatIsNotAUuid() throws Exception {
        mockMvc.perform(get("/api/visitors/not-a-uuid/assignments"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("visitorId must be a UUID"));
    }
}
