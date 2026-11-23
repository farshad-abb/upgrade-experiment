package com.doodle.experiment.experiment;

import java.security.SecureRandom;
import java.time.Clock;
import java.util.random.RandomGenerator;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExperimentConfig {

    /** Every variant has the same chance: 50/50 for two variants. */
    /** plain independent randomization, a small imbalance is expected **/
    @Bean
    VariantPicker variantPicker() {
        RandomGenerator random = new SecureRandom();
        return variants -> variants.get(random.nextInt(variants.size()));
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
