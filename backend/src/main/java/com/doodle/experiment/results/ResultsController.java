package com.doodle.experiment.results;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ResultsController {

    private final ResultsService results;

    public ResultsController(ResultsService results) {
        this.results = results;
    }

    @GetMapping("/api/experiments/{experimentKey}/results")
    public ExperimentResults results(@PathVariable String experimentKey) {
        return results.results(experimentKey);
    }
}
