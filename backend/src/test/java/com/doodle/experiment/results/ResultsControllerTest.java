package com.doodle.experiment.results;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import com.doodle.experiment.web.VisitorIds;
import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

/** Uses a fresh application context, so events from other test classes do not change the numbers. */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class ResultsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void countsAVisitorWhoSawAndClickedOnce() throws Exception {
        String visitorId = UUID.randomUUID().toString();
        String variant = JsonPath.read(
                mockMvc.perform(get("/api/visitors/" + visitorId + "/assignments"))
                        .andReturn().getResponse().getContentAsString(),
                "$.upgrade-button");
        for (String event : new String[] {"button_seen", "button_seen", "button_clicked", "button_clicked"}) {
            mockMvc.perform(post("/api/events")
                            .cookie(new Cookie(VisitorIds.COOKIE, visitorId))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"event": "%s", "experimentKey": "upgrade-button"}
                                    """.formatted(event)))
                    .andExpect(status().isNoContent());
        }

        String other = variant.equals("A") ? "B" : "A";
        mockMvc.perform(get("/api/experiments/upgrade-button/results"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {"variants": {
                          "%s": {"seen": 1, "clicked": 1, "clickRate": 1.0},
                          "%s": {"seen": 0, "clicked": 0, "clickRate": 0.0}
                        }}
                        """.formatted(variant, other)));
    }
}
