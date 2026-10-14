package com.gdamiens.website.support;

import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Calls the application under test over HTTP, as a client at its own IP address ({@code X-Forwarded-For}, trusted
 * from the loopback like from nginx): each {@code Api} has its own per-IP rate limits.
 */
public final class Api {

    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    private final String baseUrl;

    private final String ip;

    private final String token;

    private final java.util.Map<String, String> headers;

    private Api(String baseUrl, String ip, String token, java.util.Map<String, String> headers) {
        this.baseUrl = baseUrl;
        this.ip = ip;
        this.token = token;
        this.headers = headers;
    }

    public Api(int port) {
        this("http://localhost:" + port, randomIp(), null, java.util.Map.of());
    }

    /** Same client with a bearer token */
    public Api as(String token) {
        return new Api(baseUrl, ip, token, headers);
    }

    /** Same client from [ip] */
    public Api from(String ip) {
        return new Api(baseUrl, ip, token, headers);
    }

    /** Same client sending [name]: [value] with each request (e.g. the app's X-App-Version) */
    public Api withHeader(String name, String value) {
        java.util.Map<String, String> more = new java.util.HashMap<>(headers);
        more.put(name, value);
        return new Api(baseUrl, ip, token, java.util.Map.copyOf(more));
    }

    public String ip() {
        return ip;
    }

    public record Response(int status, HttpHeaders headers, String body) {

        public JsonNode json() {
            return Fixtures.JSON.readTree(body);
        }

        public String header(String name) {
            return headers.firstValue(name).orElse(null);
        }
    }

    public Response get(String path) {
        return send(request(path).GET());
    }

    public Response post(String path, Object body) {
        return send(request(path).header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body instanceof String text ? text : Fixtures.JSON.writeValueAsString(body))));
    }

    public Response post(String path) {
        return send(request(path).POST(HttpRequest.BodyPublishers.noBody()));
    }

    public Response delete(String path) {
        return send(request(path).DELETE());
    }

    /** CORS preflight from [origin] */
    public Response preflight(String path, String origin, String method) {
        return send(request(path).header("Origin", origin).header("Access-Control-Request-Method", method)
            .method("OPTIONS", HttpRequest.BodyPublishers.noBody()));
    }

    private HttpRequest.Builder request(String path) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(baseUrl + path))
            .timeout(Duration.ofSeconds(30))
            .header("X-Forwarded-For", ip)
            .header("Accept", "application/json");
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        headers.forEach(builder::header);
        return builder;
    }

    private static Response send(HttpRequest.Builder request) {
        try {
            HttpResponse<String> response = HTTP.send(request.build(), HttpResponse.BodyHandlers.ofString());
            return new Response(response.statusCode(), response.headers(), response.body());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    /** In 198.18.0.0/15 (benchmarking): never a proxy address Tomcat would skip */
    private static String randomIp() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return "198.18." + random.nextInt(256) + "." + random.nextInt(1, 255);
    }
}
