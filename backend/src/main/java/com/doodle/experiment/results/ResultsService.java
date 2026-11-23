package com.doodle.experiment.results;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.doodle.experiment.event.EventStore;
import com.doodle.experiment.experiment.Experiment;
import com.doodle.experiment.experiment.ExperimentRegistry;
import com.doodle.experiment.experiment.Variant;
import com.doodle.experiment.web.NotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ResultsService {

    private final ExperimentRegistry registry;
    private final EventStore store;

    public ResultsService(ExperimentRegistry registry, EventStore store) {
        this.registry = registry;
        this.store = store;
    }

    /** Every variant is in the result, also when it has no visitors yet. */
    public ExperimentResults results(String experimentKey) {
        Experiment experiment = registry.find(experimentKey)
                .orElseThrow(() -> new NotFoundException("Unknown experiment: " + experimentKey));

        Map<Variant, VariantResult> variants = new LinkedHashMap<>();
        for (Variant variant : experiment.variants()) {
            Set<String> seen = store.visitors(experimentKey, variant, experiment.exposureEvent());
            // Only count clicks of visitors who also saw the button, so the rate can never be above 1.
            long clicked = store.visitors(experimentKey, variant, experiment.conversionEvent()).stream()
                    .filter(seen::contains)
                    .count();
            variants.put(variant, VariantResult.of(seen.size(), clicked));
        }
        return new ExperimentResults(experimentKey, variants);
    }
}
