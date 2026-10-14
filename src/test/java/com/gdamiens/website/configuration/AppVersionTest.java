package com.gdamiens.website.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AppVersionTest {

    @SuppressWarnings("unchecked")
    private static AppVersion of(BuildProperties build) {
        ObjectProvider<BuildProperties> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(build);
        return new AppVersion(provider);
    }

    @Test
    void isTheReleaseOfTheJar() {
        Properties properties = new Properties();
        properties.setProperty("app.version", "0.6.0");

        assertThat(of(new BuildProperties(properties)).get()).isEqualTo("0.6.0");
    }

    @Test
    void isDevWithoutRelease() {
        assertThat(of(null).get()).isEqualTo("dev");
        assertThat(of(new BuildProperties(new Properties())).get()).isEqualTo("dev");
    }
}
