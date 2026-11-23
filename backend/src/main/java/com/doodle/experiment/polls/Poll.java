package com.doodle.experiment.polls;

/**
 * The fields of a poll that are needs.initiated is the creation time as Unix milliseconds.
 */
public record Poll(String id, String title, long initiated) {
}
