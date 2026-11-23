package com.doodle.experiment.experiment;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

/**
 * The running experiments and the events we accept. Hard-coded: there is no admin UI in this challenge.
 */
@Component
public class ExperimentRegistry {

    public static final String UPGRADE_BUTTON = "upgrade-button";

    private final Map<String, Experiment> experiments = new LinkedHashMap<>();

    public ExperimentRegistry() {
        register(new Experiment(UPGRADE_BUTTON, List.of(Variant.A, Variant.B), "button_seen", "button_clicked"));
    }

    private void register(Experiment experiment) {
        experiments.put(experiment.key(), experiment);
    }

    public Collection<Experiment> all() {
        return experiments.values();
    }

    public Optional<Experiment> find(String key) {
        return Optional.ofNullable(experiments.get(key));
    }
}
