package com.doodle.experiment.polls;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Comparator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/**
 * Loads the polls once at startup. All polls are kept: the {@code hidden} field is ignored on purpose.
 */
@Repository
public class PollRepository {

    private final List<Poll> newestFirst;

    public PollRepository(ObjectMapper objectMapper) {
        ClassPathResource source = new ClassPathResource("polls.json");
        try (InputStream in = source.getInputStream()) {
            List<RawPoll> raw = objectMapper.readValue(in, new TypeReference<>() {});
            this.newestFirst = raw.stream()
                    .map(p -> new Poll(p.id(), p.title(), p.initiated()))
                    .sorted(Comparator.comparingLong(Poll::initiated).reversed())
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not load " + source.getPath(), e);
        }
    }

    public List<Poll> findAllNewestFirst() {
        return newestFirst;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record RawPoll(String id, String title, long initiated) {
    }
}
