package com.doodle.experiment.polls;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/polls")
public class PollController {

    private final PollRepository polls;

    public PollController(PollRepository polls) {
        this.polls = polls;
    }

    @GetMapping
    public List<Poll> list() {
        return polls.findAllNewestFirst();
    }
}
