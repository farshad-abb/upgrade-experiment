package com.doodle.experiment.event;

import java.time.Clock;

import com.doodle.experiment.experiment.AssignmentService;
import com.doodle.experiment.experiment.Experiment;
import com.doodle.experiment.experiment.ExperimentRegistry;
import com.doodle.experiment.experiment.Variant;
import com.doodle.experiment.web.BadRequestException;
import org.springframework.stereotype.Service;

@Service
public class EventService {

    private final ExperimentRegistry registry;
    private final AssignmentService assignments;
    private final EventStore store;
    private final Clock clock;

    public EventService(ExperimentRegistry registry, AssignmentService assignments, EventStore store, Clock clock) {
        this.registry = registry;
        this.assignments = assignments;
        this.store = store;
        this.clock = clock;
    }

    /**
     * Records an event for the visitor's variant, which we look up here: the client never says which
     * variant it saw.
     *
     * @throws BadRequestException if the event, the experiment or the visitor is unknown, or they do not match
     */
    public void record(String visitorId, String event, String experimentKey) {
        Experiment experiment = registry.find(experimentKey)
                .orElseThrow(() -> new BadRequestException("Unknown experiment: " + experimentKey));
        if (!experiment.hasEvent(event)) {
            throw new BadRequestException(
                    "Event '%s' does not belong to experiment '%s'".formatted(event, experimentKey));
        }
        Variant variant = assignments.find(visitorId, experimentKey)
                .orElseThrow(() -> new BadRequestException(
                        "Visitor is not assigned to experiment '%s'".formatted(experimentKey)));
        store.addExperimentEvent(experimentKey, variant, event, visitorId, clock.instant());
    }
}
