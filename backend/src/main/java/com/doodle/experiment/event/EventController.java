package com.doodle.experiment.event;

import com.doodle.experiment.web.BadRequestException;
import com.doodle.experiment.web.VisitorIds;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EventController {

    private final EventService events;

    public EventController(EventService events) {
        this.events = events;
    }

    /**
     * The visitor comes only from the {@code visitor_id} cookie Nextjs sets, so a caller can only
     * record events for itself. 204 also for a repeated event: it is stored once per visitor, so retries are safe.
     */
    @PostMapping("/api/events")
    public ResponseEntity<Void> record(
            @CookieValue(name = VisitorIds.COOKIE, required = false) String cookieVisitorId,
            @Valid @RequestBody EventRequest request) {
        if (cookieVisitorId == null || cookieVisitorId.isBlank()) {
            throw new BadRequestException("Missing " + VisitorIds.COOKIE + " cookie");
        }
        events.record(VisitorIds.require(cookieVisitorId), request.event(), request.experimentKey());
        return ResponseEntity.noContent().build();
    }
}
