package com.doodle.experiment.results;

import static com.doodle.experiment.experiment.ExperimentRegistry.UPGRADE_BUTTON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.util.Map;

import com.doodle.experiment.event.EventService;
import com.doodle.experiment.event.EventStore;
import com.doodle.experiment.experiment.AssignmentService;
import com.doodle.experiment.experiment.ExperimentRegistry;
import com.doodle.experiment.experiment.Variant;
import com.doodle.experiment.web.NotFoundException;
import org.junit.jupiter.api.Test;

class ResultsServiceTest {

    private static final String SEEN = "button_seen";
    private static final String CLICKED = "button_clicked";

    private final ExperimentRegistry registry = new ExperimentRegistry();
    private final EventStore store = new EventStore();
    private final ResultsService results = new ResultsService(registry, store);

    /** The picker returns whatever {@link #visitor} sets, so each test decides who is in A and who in B. */
    private Variant nextVariant;
    private final AssignmentService assignments = new AssignmentService(registry, variants -> nextVariant);
    private final EventService events = new EventService(registry, assignments, store, Clock.systemUTC());

    private void visitor(String id, Variant variant) {
        nextVariant = variant;
        assignments.assignAll(id);
    }

    @Test
    void returnsZerosForEveryVariantWhenNobodyVisited() {
        ExperimentResults result = results.results(UPGRADE_BUTTON);

        assertThat(result.experimentKey()).isEqualTo(UPGRADE_BUTTON);
        assertThat(result.variants()).containsExactly(
                Map.entry(Variant.A, new VariantResult(0, 0, 0.0)),
                Map.entry(Variant.B, new VariantResult(0, 0, 0.0)));
    }

    @Test
    void countsSeenAndClickedPerVariant() {
        visitor("a1", Variant.A);
        visitor("a2", Variant.A);
        visitor("a3", Variant.A);
        visitor("a4", Variant.A);
        visitor("b1", Variant.B);
        visitor("b2", Variant.B);
        for (String id : new String[] {"a1", "a2", "a3", "a4", "b1", "b2"}) {
            events.record(id, SEEN, UPGRADE_BUTTON);
        }
        events.record("a1", CLICKED, UPGRADE_BUTTON);
        events.record("b1", CLICKED, UPGRADE_BUTTON);
        events.record("b2", CLICKED, UPGRADE_BUTTON);

        ExperimentResults result = results.results(UPGRADE_BUTTON);

        assertThat(result.variants().get(Variant.A)).isEqualTo(new VariantResult(4, 1, 0.25));
        assertThat(result.variants().get(Variant.B)).isEqualTo(new VariantResult(2, 2, 1.0));
    }

    @Test
    void countsEachVisitorOnceWhenTheyReloadOrClickAgain() {
        visitor("a1", Variant.A);
        for (int i = 0; i < 3; i++) {
            events.record("a1", SEEN, UPGRADE_BUTTON);
            events.record("a1", CLICKED, UPGRADE_BUTTON);
        }

        assertThat(results.results(UPGRADE_BUTTON).variants().get(Variant.A))
                .isEqualTo(new VariantResult(1, 1, 1.0));
    }

    @Test
    void ignoresClicksFromVisitorsWhoWereNotSeen() {
        visitor("a1", Variant.A);
        events.record("a1", CLICKED, UPGRADE_BUTTON);

        assertThat(results.results(UPGRADE_BUTTON).variants().get(Variant.A))
                .isEqualTo(new VariantResult(0, 0, 0.0));
    }

    @Test
    void failsForAnUnknownExperiment() {
        assertThatThrownBy(() -> results.results("nope")).isInstanceOf(NotFoundException.class);
    }
}
