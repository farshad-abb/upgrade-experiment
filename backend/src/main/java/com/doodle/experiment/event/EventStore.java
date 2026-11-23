package com.doodle.experiment.event;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.doodle.experiment.experiment.Variant;
import org.springframework.stereotype.Component;

/**
 * Keeps events in memory, once per visitor: a visitor who reloads or clicks twice is stored once,
 * with the time of the first event. That makes recording idempotent and the counts unique by design.
 */
@Component
public class EventStore {

    private record ExperimentEventKey(String experimentKey, Variant variant, String event) {
    }

    private final Map<ExperimentEventKey, Map<String, Instant>> experimentEvents = new ConcurrentHashMap<>();

    public void addExperimentEvent(String experimentKey, Variant variant, String event, String visitorId, Instant at) {
        experimentEvents
                .computeIfAbsent(new ExperimentEventKey(experimentKey, variant, event), k -> new ConcurrentHashMap<>())
                .putIfAbsent(visitorId, at);
    }

    /** Visitors in this variant who sent this event at least once. */
    public Set<String> visitors(String experimentKey, Variant variant, String event) {
        return Set.copyOf(experimentEvents
                .getOrDefault(new ExperimentEventKey(experimentKey, variant, event), Map.of())
                .keySet());
    }
}
