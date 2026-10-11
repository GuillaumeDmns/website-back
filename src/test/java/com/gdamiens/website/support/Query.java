package com.gdamiens.website.support;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Query string parameters, decoded */
final class Query {

    private Query() {
    }

    static Map<String, List<String>> parse(String rawQuery) {
        Map<String, List<String>> params = new LinkedHashMap<>();
        if (rawQuery == null || rawQuery.isEmpty()) {
            return params;
        }
        for (String pair : rawQuery.split("&")) {
            int equals = pair.indexOf('=');
            String name = decode(equals < 0 ? pair : pair.substring(0, equals));
            String value = equals < 0 ? "" : decode(pair.substring(equals + 1));
            params.computeIfAbsent(name, key -> new ArrayList<>()).add(value);
        }
        return params;
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
