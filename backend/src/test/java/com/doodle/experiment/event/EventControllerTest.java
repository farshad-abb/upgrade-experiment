package com.doodle.experiment.event;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import com.doodle.experiment.web.VisitorIds;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
class EventControllerTest {

    @Autowired
    MockMvc mockMvc;

    private String visitorId;

    @BeforeEach
    void assignVisitor() throws Exception {
        visitorId = UUID.randomUUID().toString();
        mockMvc.perform(get("/api/visitors/" + visitorId + "/assignments")).andExpect(status().isOk());
    }

    /** Sends as the assigned visitor, identified by the cookie like a browser. */
    private ResultActions send(String json) throws Exception {
        return sendWithCookie(visitorId, json);
    }

    @Test
    void acceptsAnExperimentEvent() throws Exception {
        send("""
                {"event": "button_seen", "experimentKey": "upgrade-button"}
                """)
                .andExpect(status().isNoContent());
    }

    @Test
    void rejectsAVisitorThatWasNeverAssigned() throws Exception {
        sendWithCookie(UUID.randomUUID().toString(), """
                {"event": "button_seen", "experimentKey": "upgrade-button"}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Visitor is not assigned to experiment 'upgrade-button'"));
    }

    /** A cross-site HTML form can only post form or text bodies; refusing them blocks that CSRF route. */
    @Test
    void rejectsAFormPost() throws Exception {
        mockMvc.perform(post("/api/events")
                        .cookie(new Cookie(VisitorIds.COOKIE, visitorId))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content("event=button_clicked&experimentKey=upgrade-button"))
                .andExpect(status().isUnsupportedMediaType());
    }

    private ResultActions sendWithCookie(String cookieValue, String json) throws Exception {
        return mockMvc.perform(post("/api/events")
                .cookie(new Cookie(VisitorIds.COOKIE, cookieValue))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }
}
