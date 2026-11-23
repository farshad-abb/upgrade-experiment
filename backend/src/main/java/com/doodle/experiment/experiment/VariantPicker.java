package com.doodle.experiment.experiment;

import java.util.List;

/** Picks the variant for a visitor who has none yet. An interface so tests can make it predictable. */
@FunctionalInterface
public interface VariantPicker {

    Variant pick(List<Variant> variants);
}
