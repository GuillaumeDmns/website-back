package com.gdamiens.website.configuration;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;

/** The release running: the git tag's vX.Y.Z of the deployed jar (pom.xml's app.version), "dev" otherwise */
@Component
public class AppVersion {

    private final String version;

    public AppVersion(ObjectProvider<BuildProperties> buildProperties) {
        BuildProperties build = buildProperties.getIfAvailable();
        String fromBuild = build == null ? null : build.get("app.version");
        this.version = fromBuild == null || fromBuild.isBlank() ? "dev" : fromBuild;
    }

    public String get() {
        return version;
    }
}
