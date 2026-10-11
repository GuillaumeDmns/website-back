package com.gdamiens.website.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** {@code @Scheduled} tasks run unless {@code application.scheduling.enabled} is false */
@Configuration
@EnableScheduling
@ConditionalOnBooleanProperty(name = "application.scheduling.enabled", matchIfMissing = true)
public class SchedulerConfig {}
