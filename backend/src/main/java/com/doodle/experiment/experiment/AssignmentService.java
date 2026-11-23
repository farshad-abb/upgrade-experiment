package com.doodle.experiment.experiment;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

/**
 * Gives each visitor a variant per experiment and remembers it, so the visitor always gets the same one.
 * Kept in memory: assignments are lost when the server restarts.
 */
@Service
public class AssignmentService {

    /** visitorId -> (experimentKey -> variant). */
    private final Map<String, Map<String, Variant>> assignments = new ConcurrentHashMap<>();

    private final ExperimentRegistry registry;
    private final VariantPicker picker;

    public AssignmentService(ExperimentRegistry registry, VariantPicker picker) {
        this.registry = registry;
        this.picker = picker;
    }

    /**
     * Returns the visitor's variant for every running experiment, assigning the missing ones.
     * computeIfAbsent is atomic, so two parallel first requests (e.g. two tabs) get the same answer.
     */
    public Map<String, Variant> assignAll(String visitorId) {
        Map<String, Variant> forVisitor = assignments.computeIfAbsent(visitorId, id -> new ConcurrentHashMap<>());
        Map<String, Variant> result = new LinkedHashMap<>();
        for (Experiment experiment : registry.all()) {
            result.put(experiment.key(),
                    forVisitor.computeIfAbsent(experiment.key(), key -> picker.pick(experiment.variants())));
        }
        return result;
    }

    public Optional<Variant> find(String visitorId, String experimentKey) {
        return Optional.ofNullable(assignments.get(visitorId)).map(m -> m.get(experimentKey));
    }
}
