package com.gdamiens.website.support;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/** Files of {@code src/test/resources} */
public final class Fixtures {

    public static final JsonMapper JSON = JsonMapper.builder().build();

    private Fixtures() {
    }

    public static String read(String path) {
        try (InputStream in = Fixtures.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalArgumentException("No test resource " + path);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static JsonNode json(String path) {
        return JSON.readTree(read(path));
    }
}
