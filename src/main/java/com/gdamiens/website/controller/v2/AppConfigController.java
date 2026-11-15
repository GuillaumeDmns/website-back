package com.gdamiens.website.controller.v2;

import com.gdamiens.website.configuration.ApplicationProperties;
import com.gdamiens.website.controller.object.v2.AppConfig;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * What the apps ask at start-up, without a token (an app too old to sign in must still learn it has to be updated):
 * the oldest supported versions, the features switched off, a message for everyone. Read from the configuration
 * ({@code application.apps}): changed in the production file, applied by a restart.
 */
@RestController
@RequestMapping("/api/v2")
public class AppConfigController {

    private final AppConfig config;

    public AppConfigController(ApplicationProperties properties) {
        ApplicationProperties.Apps apps = properties.getApps();
        Map<String, String> notice = new LinkedHashMap<>();
        apps.getNotice().forEach((language, text) -> {
            if (StringUtils.isNotBlank(text)) {
                notice.put(language, text.strip());
            }
        });
        this.config = new AppConfig(Map.copyOf(apps.getMinVersions()), List.copyOf(apps.getDisabledFeatures()), notice);
    }

    @GetMapping("/app-config")
    @Operation(summary = "Oldest supported app versions per platform, features switched off, message for the home page; public")
    public ResponseEntity<AppConfig> appConfig() {
        return ResponseEntity.ok().cacheControl(CacheControl.maxAge(Duration.ofMinutes(5)).cachePublic()).body(config);
    }
}
