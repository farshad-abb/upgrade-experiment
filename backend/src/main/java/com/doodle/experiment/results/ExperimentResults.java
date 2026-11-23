package com.doodle.experiment.results;

import java.util.Map;

import com.doodle.experiment.experiment.Variant;

public record ExperimentResults(String experimentKey, Map<Variant, VariantResult> variants) {
}
