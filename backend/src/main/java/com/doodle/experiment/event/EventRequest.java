package com.doodle.experiment.event;

import jakarta.validation.constraints.NotBlank;

/** The visitor is not part of the body: it comes from the {@code visitor_id} cookie. */
public record EventRequest(@NotBlank String event, @NotBlank String experimentKey) {
}
