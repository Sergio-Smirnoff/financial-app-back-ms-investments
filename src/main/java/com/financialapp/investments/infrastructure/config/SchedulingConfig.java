package com.financialapp.investments.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Enables Spring scheduling for the application's background jobs
 * (market sync, price refresh, portfolio snapshots).
 *
 * <p>Profile-gated to {@code !test} so that {@code @SpringBootTest} integration
 * tests start without the schedulers firing (e.g. the market-discovery job runs
 * almost immediately on context start and would issue real IOL/use-case calls
 * mid-test). Production runs every non-test profile, so behaviour is unchanged.
 */
@Configuration
@EnableScheduling
@Profile("!test")
public class SchedulingConfig {
}
