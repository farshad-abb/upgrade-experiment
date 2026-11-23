package com.doodle.experiment.experiment;

import java.util.List;
import java.util.Set;

/**
 * An A/B experiment.
 *
 * @param key             stable id used in URLs and event payloads
 * @param variants        variants a visitor can be assigned to, each with the same chance
 * @param exposureEvent   event sent when the visitor saw the experiment's UI ("seen")
 * @param conversionEvent event sent when the visitor did what we want ("clicked")
 */
public record Experiment(String key, List<Variant> variants, String exposureEvent, String conversionEvent) {

    public Set<String> events() {
        return Set.of(exposureEvent, conversionEvent);
    }

    public boolean hasEvent(String event) {
        return events().contains(event);
    }
}
