package com.doodle.experiment.experiment;

import java.util.Map;

import com.doodle.experiment.web.VisitorIds;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AssignmentController {

    private final AssignmentService assignments;

    public AssignmentController(AssignmentService assignments) {
        this.assignments = assignments;
    }

    /**
     * Returns the visitor's variant for every running experiment, e.g. {"upgrade-button": "B"}.
     * The first call assigns; later calls return the same values. Not cacheable: it is per visitor.
     */
    @GetMapping("/api/visitors/{visitorId}/assignments")
    public ResponseEntity<Map<String, Variant>> assignments(@PathVariable String visitorId) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(assignments.assignAll(VisitorIds.require(visitorId)));
    }
}
