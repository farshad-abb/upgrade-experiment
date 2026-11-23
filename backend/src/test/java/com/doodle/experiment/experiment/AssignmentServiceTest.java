package com.doodle.experiment.experiment;

import static com.doodle.experiment.experiment.ExperimentRegistry.UPGRADE_BUTTON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

class AssignmentServiceTest {

    private final ExperimentRegistry registry = new ExperimentRegistry();

    @Test
    void splitsNewVisitorsRoughlyFiftyFifty() {
        AssignmentService service = new AssignmentService(registry, new ExperimentConfig().variantPicker());
        int visitors = 10_000;

        long a = 0;
        for (int i = 0; i < visitors; i++) {
            if (service.assignAll(UUID.randomUUID().toString()).get(UPGRADE_BUTTON) == Variant.A) {
                a++;
            }
        }

        // With 10,000 fair coin flips the share of A is 50% +/- 1% (2 standard deviations) 95% of the time.
        // 3% keeps the test from failing by bad luck (more than 6 standard deviations).
        assertThat((double) a / visitors).isCloseTo(0.5, within(0.03));
    }

    @Test
    void picksOnlyOnceForAVisitor() {
        AtomicInteger picks = new AtomicInteger();
        VariantPicker countingPicker = variants -> {
            picks.incrementAndGet();
            return Variant.B;
        };
        AssignmentService service = new AssignmentService(registry, countingPicker);

        service.assignAll("v1");
        service.assignAll("v1");

        assertThat(picks).hasValue(1);
        assertThat(service.find("v1", UPGRADE_BUTTON)).contains(Variant.B);
    }
}
